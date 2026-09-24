package com.opt.backend.dto;

import com.opt.backend.entity.Team;

import java.util.UUID;

public record LeaderboardEntryDto(UUID userId, String fullName, Team team, int pixelCount) {
}
