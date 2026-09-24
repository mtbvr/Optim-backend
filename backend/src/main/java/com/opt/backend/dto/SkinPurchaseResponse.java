package com.opt.backend.dto;

import java.util.List;

public record SkinPurchaseResponse(PlayerStateDto me, List<AchievementDefinition> newAchievements) {
}
