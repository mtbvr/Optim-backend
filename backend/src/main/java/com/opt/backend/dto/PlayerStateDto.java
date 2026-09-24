package com.opt.backend.dto;

import com.opt.backend.entity.Team;
import com.opt.backend.entity.User;

public record PlayerStateDto(Team team, int points, int bombCharges, int fortressCharges, String selectedSkin) {

    public static PlayerStateDto from(User user) {
        return new PlayerStateDto(user.getTeam(), user.getPoints(), user.getBombCharges(),
                user.getFortressCharges(), user.getSelectedSkin());
    }
}
