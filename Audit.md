# Pixel Wars — Audit de Performance Backend

Backend Java 21 / Spring Boot d'un jeu multijoueur de placement de pixels par équipes
(façon *r/place*). Ce document présente l'audit de performance du chemin critique de l'API. Chaque version ultérieure est mesurée avec le même protocole et vient enrichir
la synthèse.

---

## Contexte & Hot Path

Pixel Wars oppose des équipes sur un plateau de **120 × 80 = 9 600 cases**. Deux routes
concentrent la quasi-totalité du trafic et constituent le **hot path** — seule cible de l'audit :

| Route | Rôle | Nature de la charge |
|---|---|---|
| `GET /api/pixels` | Lecture continue du plateau | Très fréquente (rendu client) |
| `POST /api/pixels` | Placement d'un pixel | Écriture concurrente entre joueurs |

---

## Synthèse des performances par version

Mesure macro via **vegeta** (charge HTTP réelle), selon deux profils :

- **Séquentiel** — 1 requête à la fois (latence à vide).
- **Concurrent** — 25 comptes distincts × 3 rafales (comportement sous contention).

Latence moyenne, en millisecondes (plus bas = meilleur) :

| Version | Stratégie | GET séq. | GET conc. | POST séq. | POST conc. |
|---|---|---:|---:|---:|---:|
| **V0** | Baseline (état actuel) | 16.3 | 48.8 | 34.1 | 216.3 |

> Signal le plus fort du tableau : le `POST` concurrent explose (34 → 216 ms, ×6,3) là où le
> séquentiel reste bas. C'est la signature d'une sérialisation des écritures — confirmée au
> diagnostic.

---

## Diagnostic de la V0

Deux goulots indépendants, chacun confirmé par un outil dédié.

Rapport de benchmark complet : [`benchmark_v0.pdf`](./benchmark_v0.pdf).

### 1. POST — contention de verrou

`PixelServiceImpl.placePixel()` est `synchronized` pendant toute son exécution.

Un seul joueur peut poser un pixel à la fois. Les autres doivent attendre que le traitement précédent soit terminé.

Le verrou reste actif pendant plusieurs opérations : validation, écriture PostgreSQL, calcul des combos/captures et diffusion WebSocket.

Confirmé à deux niveaux indépendants :

- **Charge vegeta** — lorsque plusieurs POST sont envoyés en même temps, les requêtes deviennent beaucoup plus lentes et les performances chutent, avec une latence d’environ **216 ms**.
  Visible dans `benchmark_v0.pdf`, chapitres « Graphique - POST : séquentiel vs concurrent » et « Graphique - POST : latence par ordre de complétion ». La diagramme est en dent de scie
- **JDK Flight Recorder + flamegraph** (async-profiler) sous charge POST concurrente : **75 événements `JavaMonitorEnter`**, correspondant aux 75 placements. Le flamegraph montre que les requêtes passent leur temps à attendre.
  Visible dans `benchmark_v0.pdf`, chapitre « Graphique - Flamegraph : contention sur verrous (POST concurrent) ».

### 2. GET — parcours de toutes les cases & réallocations

`BoardGrid.snapshot()` parcourt les 9 600 cases du plateau à chaque appel, même si seulement quelques pixels sont réellement posés.

Le programme vérifie donc beaucoup de cases inutiles. Le temps de traitement dépend ainsi de la taille totale du plateau, et non du nombre de pixels présents.

La méthode utilise également une ArrayList sans capacité initiale. La liste doit donc être agrandie plusieurs fois pendant l’ajout des pixels, ce qui provoque des réallocations.

Mesuré isolément (hyperfine, 200 000 appels, 15 runs) : 2 784 ms ± 285 ms, soit environ
13,9 µs par appel. C'est long pour une méthode appelée à chaque lecture du plateau — le coût reste noyé dans le bruit réseau à l'échelle HTTP actuelle, mais il est réel et croît avec la taille du plateau.

Visible dans `benchmark_v0.pdf`, chapitre « Graphique - hyperfine : micro-benchmark de BoardGrid.snapshot() ».

**Où ça devient vraiment long.** Pour vérifier que le coût dépend bien de la surface du
plateau et non du nombre de pixels posés, le même appel a été rejoué sur des plateaux plus
grands (500×500, 2000×2000, 6000×6000), en gardant le nombre de pixels posés strictement
fixe à 183. Résultat : la surface passe de 9 600 à 36 000 000 cases (×3 750) et le coût par
appel passe de 13,9 µs à **47,2 ms** (×3 345) — quasiment le même facteur. Les points mesurés
suivent la droite `O(n)` théorique sur 4 ordres de grandeur. Sur le plateau réel (120×80), le
coût reste négligeable ; il ne le restera plus sur un plateau nettement plus grand.

Visible dans `benchmark_v0.pdf`, chapitre « Graphique - hyperfine : passage à l'échelle de
BoardGrid.snapshot() ».

---

## Outillage & reproduction

```bash
# Démarrer la stack applicative
docker compose up -d

# Benchmark complet : charge HTTP (vegeta) + micro-benchmark (hyperfine)
make bench

# Micro-benchmark seul (hyperfine)
make bench-hyperfine

# Charges HTTP ciblées
make bench-get-sequential
make bench-get-concurrent
make bench-post-sequential
make bench-post-concurrent
```