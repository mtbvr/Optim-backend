package com.opt.backend.dto;

import com.opt.backend.entity.AchievementId;

import java.time.OffsetDateTime;

public record UnlockedAchievementDto(AchievementId id, String name, String description, OffsetDateTime unlockedAt) {
}
