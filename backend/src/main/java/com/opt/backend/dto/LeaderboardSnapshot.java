package com.opt.backend.dto;

import com.opt.backend.entity.Team;

import java.util.List;
import java.util.Map;

public record LeaderboardSnapshot(List<LeaderboardEntryDto> topPlayers, Map<Team, Integer> teamTotals) {
}
