package com.opt.backend.dto;

import com.opt.backend.entity.Team;

public record TeamPoolUpdateEvent(String type, Team team, int amount, int threshold) {

    public static TeamPoolUpdateEvent of(Team team, int amount, int threshold) {
        return new TeamPoolUpdateEvent("TEAM_POOL_UPDATE", team, amount, threshold);
    }
}
