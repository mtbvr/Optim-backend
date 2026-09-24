package com.opt.backend.dto;

import com.opt.backend.entity.Team;

public record TeamPoolTriggeredEvent(String type, Team team, long until) {

    public static TeamPoolTriggeredEvent of(Team team, long until) {
        return new TeamPoolTriggeredEvent("TEAM_POOL_TRIGGERED", team, until);
    }
}
