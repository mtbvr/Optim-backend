package com.opt.backend.dto;

import com.opt.backend.entity.Team;
import com.opt.backend.entity.User;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        Team team,
        OffsetDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getTeam(), user.getCreatedAt());
    }
}
