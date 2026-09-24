package com.opt.backend.dto;

import com.opt.backend.entity.SkinId;
import com.opt.backend.entity.Team;

import java.time.OffsetDateTime;
import java.util.List;

public record ProfileResponse(
        String fullName,
        Team team,
        OffsetDateTime createdAt,
        UserStatsDto stats,
        List<OwnedPerkDto> ownedPerks,
        List<SkinId> ownedSkins,
        List<UnlockedAchievementDto> achievements
) {
}
