package com.opt.backend.dto;

import com.opt.backend.entity.AchievementId;
import com.opt.backend.entity.SkinId;
import com.opt.backend.entity.SkinKind;

public record SkinDefinition(
        SkinId id,
        String name,
        SkinKind kind,
        String hexColor,
        int cost,
        AchievementId requiresAchievement
) {
}
