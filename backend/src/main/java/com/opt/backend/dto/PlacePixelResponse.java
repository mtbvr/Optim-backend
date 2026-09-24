package com.opt.backend.dto;

import java.util.List;

public record PlacePixelResponse(
        List<PixelDto> placedCells,
        List<PixelDto> capturedCells,
        int pointsAwarded,
        int comboPoints,
        int capturePoints,
        boolean bonusApplied,
        PlayerStateDto me,
        int cooldownSeconds,
        List<AchievementDefinition> newAchievements
) {
}
