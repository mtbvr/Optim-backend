package com.opt.backend.controller;

import com.opt.backend.common.config.PixelWarsProperties;
import com.opt.backend.common.pixelwars.BoardGrid;
import com.opt.backend.common.pixelwars.LeaderboardTracker;
import com.opt.backend.dto.GlobalStatsResponse;
import com.opt.backend.dto.PixelDto;
import com.opt.backend.entity.Team;
import com.opt.backend.repository.PixelRepository;
import com.opt.backend.repository.UserRepository;
import com.opt.backend.repository.UserStatsRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private static final int PREVIEW_SAMPLE_STEP = 2;

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final LeaderboardTracker leaderboardTracker;
    private final PixelRepository pixelRepository;
    private final PixelWarsProperties properties;
    private final BoardGrid boardGrid;

    public StatsController(UserRepository userRepository, UserStatsRepository userStatsRepository,
                            LeaderboardTracker leaderboardTracker, PixelRepository pixelRepository,
                            PixelWarsProperties properties, BoardGrid boardGrid) {
        this.userRepository = userRepository;
        this.userStatsRepository = userStatsRepository;
        this.leaderboardTracker = leaderboardTracker;
        this.pixelRepository = pixelRepository;
        this.properties = properties;
        this.boardGrid = boardGrid;
    }

    @GetMapping("/global")
    public ResponseEntity<GlobalStatsResponse> getGlobalStats() {
        var preview = pixelRepository.findAll().stream()
                .filter(pixel -> pixel.getId().getX() % PREVIEW_SAMPLE_STEP == 0 && pixel.getId().getY() % PREVIEW_SAMPLE_STEP == 0)
                .map(PixelDto::from)
                .toList();

        return ResponseEntity.ok(new GlobalStatsResponse(
                userRepository.count(),
                userStatsRepository.sumPixelsPlaced(),
                leaderboardTracker.snapshot().teamTotals(),
                properties.getWidth(),
                properties.getHeight(),
                preview
        ));
    }

    @GetMapping(value = "/board-export", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> exportBoardCsv() {
        String csv = "x,y,team\n";
        for (int x = 0; x < properties.getWidth(); x++) {
            for (int y = 0; y < properties.getHeight(); y++) {
                Team team = boardGrid.get(x, y);
                if (team != null) {
                    csv += x + "," + y + "," + team + "\n";
                }
            }
        }
        return ResponseEntity.ok(csv);
    }
}
