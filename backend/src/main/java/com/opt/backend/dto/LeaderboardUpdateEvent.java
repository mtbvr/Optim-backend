package com.opt.backend.dto;

import com.opt.backend.entity.Team;

import java.util.List;
import java.util.Map;

public record LeaderboardUpdateEvent(String type, List<LeaderboardEntryDto> topPlayers, Map<Team, Integer> teamTotals) {

    public static LeaderboardUpdateEvent of(LeaderboardSnapshot snapshot) {
        return new LeaderboardUpdateEvent("LEADERBOARD_UPDATE", snapshot.topPlayers(), snapshot.teamTotals());
    }
}
