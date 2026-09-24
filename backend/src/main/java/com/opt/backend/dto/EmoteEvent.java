package com.opt.backend.dto;

import com.opt.backend.entity.Team;

public record EmoteEvent(String type, String fullName, Team team, String emoji) {

    public static EmoteEvent of(String fullName, Team team, String emoji) {
        return new EmoteEvent("EMOTE", fullName, team, emoji);
    }
}
