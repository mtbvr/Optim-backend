package com.opt.backend.dto;

import com.opt.backend.entity.Team;

import java.util.List;
import java.util.Map;

public record BoardResponse(
        int width,
        int height,
        int cooldownSeconds,
        Map<Team, String> teamColors,
        List<PixelDto> pixels,
        LeaderboardSnapshot leaderboard,
        PlayerStateDto me,
        BonusZoneDto bonusZone,
        TeamPoolsDto teamPools
) {
}
