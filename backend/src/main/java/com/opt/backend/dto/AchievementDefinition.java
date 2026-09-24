package com.opt.backend.dto;

import com.opt.backend.entity.AchievementId;

public record AchievementDefinition(AchievementId id, String name, String description) {
}
