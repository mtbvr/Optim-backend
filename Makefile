.ONESHELL:
SHELL := bash
.SHELLFLAGS := -eu -o pipefail -c

BASE_URL      ?= http://localhost:8090
CONTAINER     ?= opt-backend-backend-1
OUT_DIR       ?= benchmark-results

SEQ_GET_RUNS   ?= 30
SEQ_GET_WARMUP ?= 5
SEQ_POST_RUNS  ?= 10
BURST_SIZE     ?= 25
BURSTS         ?= 3
GET_BURST_GAP  ?= 1
HF_ITERATIONS  ?= 200000
HF_RUNS        ?= 15
HF_WARMUP      ?= 3

.PHONY: help auth bench-get-sequential bench-get-concurrent bench-post-sequential bench-post-concurrent bench-hyperfine bench-hyperfine-scaling bench clean

help:
	cat <<-'EOF'
	Benchmark Hot Path Pixel Wars (independant)
	==========================================================
	
	Cible : GET /api/pixels et POST /api/pixels, les deux routes les
	plus sollicitees de l'application (lecture continue du plateau par
	le client, ecriture a chaque placement).
	
	Prerequis : bash, curl, awk, docker (stack lancee via docker compose up -d),
	vegeta (charge HTTP, https://github.com/tsenart/vegeta), hyperfine
	(micro-benchmark, https://github.com/sharkdp/hyperfine), javac/java (JDK 21+).
	
	Cibles disponibles :
	  make bench                    Enchaine toutes les phases et affiche les metriques
	  make bench-get-sequential     GET en sequentiel (latence de reference) - vegeta
	  make bench-get-concurrent     GET sous charge concurrente - vegeta
	  make bench-post-sequential    POST en sequentiel (latence de reference) - vegeta
	  make bench-post-concurrent    POST sous charge concurrente - vegeta
	  make bench-hyperfine          Micro-benchmark de BoardGrid.snapshot() - hyperfine
	  make bench-hyperfine-scaling  Passage a l'echelle (surface croissante, pixels fixes)
	  make clean                    Supprime le dossier de resultats (OUT_DIR)
	
	Variables surchargeables (make VAR=valeur ...) :
	  BASE_URL=$(BASE_URL)
	  CONTAINER=$(CONTAINER)
	  OUT_DIR=$(OUT_DIR)
	  SEQ_GET_RUNS=$(SEQ_GET_RUNS)  SEQ_GET_WARMUP=$(SEQ_GET_WARMUP)
	  SEQ_POST_RUNS=$(SEQ_POST_RUNS)
	  BURST_SIZE=$(BURST_SIZE)  BURSTS=$(BURSTS)  GET_BURST_GAP=$(GET_BURST_GAP)
	  HF_ITERATIONS=$(HF_ITERATIONS)  HF_RUNS=$(HF_RUNS)  HF_WARMUP=$(HF_WARMUP)
	
	Resultats vegeta (rapports texte) et echantillons CPU/RAM dans $(OUT_DIR)/.
	EOF
	
auth:
	mkdir -p $(OUT_DIR)
	get_token() {
		email="$$1"; fullname="$$2"
		hdrs=$$(curl -s -D - -o /dev/null -X POST -H 'Content-Type: application/json' \
			-d "{\"email\":\"$$email\",\"password\":\"benchmark123\"}" \
			"$(BASE_URL)/api/auth/login")
		token=$$(printf '%s' "$$hdrs" | { grep -o 'access_token=[^;]*' || true; } | head -1 | cut -d= -f2)
		if [ -z "$${token:-}" ]; then
			hdrs=$$(curl -s -D - -o /dev/null -X POST -H 'Content-Type: application/json' \
				-d "{\"email\":\"$$email\",\"password\":\"benchmark123\",\"fullName\":\"$$fullname\"}" \
				"$(BASE_URL)/api/auth/signup")
			token=$$(printf '%s' "$$hdrs" | { grep -o 'access_token=[^;]*' || true; } | head -1 | cut -d= -f2)
		fi
		if [ -z "$${token:-}" ]; then
			echo "Echec d'authentification pour $$email" >&2
			exit 1
		fi
		printf '%s' "$$token"
	}
	get_token "hotpath-root-reader@test.local" "Hotpath Root Reader" > $(OUT_DIR)/token-reader
	get_token "hotpath-root-writer@test.local" "Hotpath Root Writer" > $(OUT_DIR)/token-writer
	for i in $$(seq 1 $$(( $(BURST_SIZE) + 5 ))); do
		get_token "hotpath-root-burst-$$i@test.local" "Hotpath Root Burst $$i" > $(OUT_DIR)/token-burst-$$i
	done
	echo "Authentification OK ($$(( $(BURST_SIZE) + 5 )) comptes de charge + 1 lecteur + 1 ecrivain)"
	
bench-get-sequential: auth
	echo "== GET /api/pixels - sequentiel (vegeta) =="
	token=$$(cat $(OUT_DIR)/token-reader)
	printf '{"method":"GET","url":"%s/api/pixels","header":{"Cookie":["access_token=%s"]}}\n' "$(BASE_URL)" "$$token" > $(OUT_DIR)/get_seq_warmup.targets.json
	vegeta attack -targets=$(OUT_DIR)/get_seq_warmup.targets.json -format=json -rate=$(SEQ_GET_WARMUP)/1s -duration=2s -workers=1 -max-workers=1 -http2=false -max-body=0 -output=$(OUT_DIR)/get_seq_warmup.bin
	printf '{"method":"GET","url":"%s/api/pixels","header":{"Cookie":["access_token=%s"]}}\n' "$(BASE_URL)" "$$token" > $(OUT_DIR)/get_seq.targets.json
	docker stats $(CONTAINER) --no-stream --format '{{.CPUPerc}},{{.MemUsage}}' > $(OUT_DIR)/resources_get_sequential.csv
	vegeta attack -targets=$(OUT_DIR)/get_seq.targets.json -format=json -rate=$(SEQ_GET_RUNS)/1s -duration=1.5s -workers=1 -max-workers=1 -http2=false -max-body=0 -output=$(OUT_DIR)/get_sequential.bin
	docker stats $(CONTAINER) --no-stream --format '{{.CPUPerc}},{{.MemUsage}}' >> $(OUT_DIR)/resources_get_sequential.csv
	vegeta report -type=text $(OUT_DIR)/get_sequential.bin
	
bench-get-concurrent: auth
	echo "== GET /api/pixels - concurrent ($(BURST_SIZE) x $(BURSTS) rafales, vegeta) =="
	token=$$(cat $(OUT_DIR)/token-reader)
	: > $(OUT_DIR)/get_concurrent.targets.json
	for i in $$(seq 1 $$(( $(BURST_SIZE) + 5 ))); do
		printf '{"method":"GET","url":"%s/api/pixels","header":{"Cookie":["access_token=%s"]}}\n' "$(BASE_URL)" "$$token" >> $(OUT_DIR)/get_concurrent.targets.json
	done
	docker stats $(CONTAINER) --no-stream --format '{{.CPUPerc}},{{.MemUsage}}' > $(OUT_DIR)/resources_get_concurrent.csv
	: > $(OUT_DIR)/get_concurrent.jsonl
	for b in $$(seq 1 $(BURSTS)); do
		nreq=$$(( ($(BURST_SIZE) + 5) * 100 ))
		vegeta attack -targets=$(OUT_DIR)/get_concurrent.targets.json -format=json -rate=$${nreq}/1s -duration=30ms \
			-workers=$(BURST_SIZE) -max-workers=$(BURST_SIZE) -http2=false -max-body=0 -output=$(OUT_DIR)/get_concurrent_b$$b.bin
		vegeta encode -to json $(OUT_DIR)/get_concurrent_b$$b.bin >> $(OUT_DIR)/get_concurrent.jsonl
		docker stats $(CONTAINER) --no-stream --format '{{.CPUPerc}},{{.MemUsage}}' >> $(OUT_DIR)/resources_get_concurrent.csv
		sleep $(GET_BURST_GAP)
	done
	vegeta report -type=text $(OUT_DIR)/get_concurrent.jsonl
	
bench-post-sequential: auth
	echo "== POST /api/pixels - sequentiel (vegeta) =="
	token=$$(cat $(OUT_DIR)/token-writer)
	cooldown=$$(curl -s -b "access_token=$$token" "$(BASE_URL)/api/pixels" | grep -o '"cooldownSeconds":[0-9]*' | grep -o '[0-9]*$$')
	echo "cooldown detecte : $${cooldown}s"
	: > $(OUT_DIR)/post_seq.targets.json
	for i in $$(seq 1 $(SEQ_POST_RUNS)); do
		x=$$(( RANDOM % 120 )); y=$$(( RANDOM % 80 ))
		body=$$(printf '{"x":%d,"y":%d,"usePerk":null}' "$$x" "$$y" | base64 -w0)
		printf '{"method":"POST","url":"%s/api/pixels","header":{"Content-Type":["application/json"],"Cookie":["access_token=%s"]},"body":"%s"}\n' \
			"$(BASE_URL)" "$$token" "$$body" >> $(OUT_DIR)/post_seq.targets.json
	done
	interval=$$(echo "$$cooldown + 0.5" | awk '{print $$1+0.5}')
	duration=$$(echo "$(SEQ_POST_RUNS) $$interval" | awk '{print $$1*$$2+2}')
	docker stats $(CONTAINER) --no-stream --format '{{.CPUPerc}},{{.MemUsage}}' > $(OUT_DIR)/resources_post_sequential.csv
	vegeta attack -targets=$(OUT_DIR)/post_seq.targets.json -format=json -rate="1/$${interval}s" -duration="$${duration}s" \
		-workers=1 -max-workers=1 -http2=false -max-body=0 -output=$(OUT_DIR)/post_sequential.bin
	docker stats $(CONTAINER) --no-stream --format '{{.CPUPerc}},{{.MemUsage}}' >> $(OUT_DIR)/resources_post_sequential.csv
	vegeta report -type=text $(OUT_DIR)/post_sequential.bin
	
bench-post-concurrent: auth
	echo "== POST /api/pixels - concurrent ($(BURST_SIZE) comptes x $(BURSTS) rafales, vegeta) =="
	first_token=$$(cat $(OUT_DIR)/token-burst-1)
	cooldown=$$(curl -s -b "access_token=$$first_token" "$(BASE_URL)/api/pixels" | grep -o '"cooldownSeconds":[0-9]*' | grep -o '[0-9]*$$')
	echo "cooldown detecte : $${cooldown}s"
	docker stats $(CONTAINER) --no-stream --format '{{.CPUPerc}},{{.MemUsage}}' > $(OUT_DIR)/resources_post_concurrent.csv
	: > $(OUT_DIR)/post_concurrent.jsonl
	for b in $$(seq 1 $(BURSTS)); do
		: > $(OUT_DIR)/post_concurrent_b$$b.targets.json
		for i in $$(seq 1 $$(( $(BURST_SIZE) + 5 ))); do
			token=$$(cat $(OUT_DIR)/token-burst-$$i)
			x=$$(( RANDOM % 120 )); y=$$(( RANDOM % 80 ))
			body=$$(printf '{"x":%d,"y":%d,"usePerk":null}' "$$x" "$$y" | base64 -w0)
			printf '{"method":"POST","url":"%s/api/pixels","header":{"Content-Type":["application/json"],"Cookie":["access_token=%s"]},"body":"%s"}\n' \
				"$(BASE_URL)" "$$token" "$$body" >> $(OUT_DIR)/post_concurrent_b$$b.targets.json
		done
		vegeta attack -targets=$(OUT_DIR)/post_concurrent_b$$b.targets.json -format=json -rate=2500/1s -duration=30ms \
			-workers=$(BURST_SIZE) -max-workers=$(BURST_SIZE) -http2=false -max-body=0 -output=$(OUT_DIR)/post_concurrent_b$$b.bin
		vegeta encode -to json $(OUT_DIR)/post_concurrent_b$$b.bin >> $(OUT_DIR)/post_concurrent.jsonl
		echo "  Rafale $$b/$(BURSTS) terminee ($(BURST_SIZE) placements concurrents)"
		docker stats $(CONTAINER) --no-stream --format '{{.CPUPerc}},{{.MemUsage}}' >> $(OUT_DIR)/resources_post_concurrent.csv
		if [ "$$b" -lt "$(BURSTS)" ]; then
			sleep $$(( cooldown + 1 ))
		fi
	done
	vegeta report -type=text $(OUT_DIR)/post_concurrent.jsonl
	
bench-hyperfine:
	mkdir -p $(OUT_DIR)/hyperfine-src $(OUT_DIR)/hyperfine-classes
	cat > $(OUT_DIR)/hyperfine-src/Snapshot.java <<-'EOF'
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Random;
	public class Snapshot {
	    static final int WIDTH = Integer.parseInt(System.getProperty("board.width", "120"));
	    static final int HEIGHT = Integer.parseInt(System.getProperty("board.height", "80"));
	    static final int PLACED_PIXELS = Integer.parseInt(System.getProperty("board.placed", "183"));
	    record PixelDto(int x, int y, int team) {}
	    static int[][] grid;
	    static List<PixelDto> snapshot() {
	        List<PixelDto> result = new ArrayList<>();
	        for (int y = 0; y < grid.length; y++) {
	            for (int x = 0; x < grid[y].length; x++) {
	                int team = grid[y][x];
	                if (team != 0) result.add(new PixelDto(x, y, team));
	            }
	        }
	        return result;
	    }
	    public static void main(String[] args) {
	        grid = new int[HEIGHT][WIDTH];
	        Random random = new Random(42);
	        int placed = 0;
	        while (placed < PLACED_PIXELS) {
	            int x = random.nextInt(WIDTH), y = random.nextInt(HEIGHT);
	            if (grid[y][x] == 0) { grid[y][x] = random.nextBoolean() ? 1 : 2; placed++; }
	        }
	        long sink = 0;
	        for (int i = 0; i < ITER; i++) sink += snapshot().size();
	        System.out.println(sink);
	    }
	    static final int ITER = Integer.parseInt(System.getProperty("hf.iterations", "200000"));
	}
	EOF
	javac -d $(OUT_DIR)/hyperfine-classes $(OUT_DIR)/hyperfine-src/Snapshot.java
	echo "== hyperfine : BoardGrid.snapshot(), scan complet + ArrayList non pre-dimensionnee =="
	hyperfine --warmup $(HF_WARMUP) --runs $(HF_RUNS) \
		-n "Snapshot (scan complet + ArrayList non pre-dimensionnee)" \
		"java -Dhf.iterations=$(HF_ITERATIONS) -cp $(OUT_DIR)/hyperfine-classes Snapshot" \
		--export-markdown $(OUT_DIR)/hyperfine-snapshot.md
	
bench-hyperfine-scaling: bench-hyperfine
	echo "== hyperfine : passage a l'echelle de BoardGrid.snapshot() (183 pixels poses fixes) =="
	hyperfine --warmup 2 --runs 8 \
		-n "120x80 (plateau reel)" "java -Dboard.width=120 -Dboard.height=80 -Dboard.placed=183 -Dhf.iterations=200000 -cp $(OUT_DIR)/hyperfine-classes Snapshot" \
		-n "500x500" "java -Dboard.width=500 -Dboard.height=500 -Dboard.placed=183 -Dhf.iterations=5000 -cp $(OUT_DIR)/hyperfine-classes Snapshot" \
		-n "2000x2000" "java -Dboard.width=2000 -Dboard.height=2000 -Dboard.placed=183 -Dhf.iterations=500 -cp $(OUT_DIR)/hyperfine-classes Snapshot" \
		-n "6000x6000" "java -Dboard.width=6000 -Dboard.height=6000 -Dboard.placed=183 -Dhf.iterations=50 -cp $(OUT_DIR)/hyperfine-classes Snapshot" \
		--export-markdown $(OUT_DIR)/hyperfine-scaling.md
	
bench: bench-get-sequential bench-get-concurrent bench-post-sequential bench-post-concurrent bench-hyperfine bench-hyperfine-scaling
	echo ""
	echo "======================================================"
	echo " Benchmark Hot Path termine."
	echo " Rapports vegeta ci-dessus ; resultat hyperfine dans $(OUT_DIR)/hyperfine-snapshot.md"
	echo " Ressources CPU/RAM (CPU%,MEM) par phase dans $(OUT_DIR)/resources_*.csv"
	echo "======================================================"
	
clean:
	rm -rf $(OUT_DIR)
	