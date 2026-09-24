package com.opt.backend.dto;

import java.util.List;

public record PerkPurchaseResponse(PlayerStateDto me, int tier, List<AchievementDefinition> newAchievements) {
}
