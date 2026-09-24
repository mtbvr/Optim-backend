package com.opt.backend.dto;

import com.opt.backend.entity.Team;

import java.util.List;
import java.util.Map;

public record GlobalStatsResponse(
        long totalPlayers,
        long totalPixelsPlacedLifetime,
        Map<Team, Integer> teamTotals,
        int boardWidth,
        int boardHeight,
        List<PixelDto> boardPreviewPixels
) {
}
