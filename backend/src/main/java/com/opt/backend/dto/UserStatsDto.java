package com.opt.backend.dto;

import com.opt.backend.entity.UserStats;

public record UserStatsDto(int pixelsPlaced, int capturesMade, int combosTriggered, int bombsUsed, int teamPoolContributions) {

    public static UserStatsDto from(UserStats stats) {
        return new UserStatsDto(stats.getPixelsPlaced(), stats.getCapturesMade(), stats.getCombosTriggered(),
                stats.getBombsUsed(), stats.getTeamPoolContributions());
    }
}
