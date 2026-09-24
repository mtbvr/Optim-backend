package com.opt.backend.dto;

import java.util.List;

public record ContributeResponse(PlayerStateDto me, TeamPoolsDto teamPools, List<AchievementDefinition> newAchievements) {
}
