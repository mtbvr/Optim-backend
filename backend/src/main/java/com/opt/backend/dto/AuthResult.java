package com.opt.backend.dto;

public record AuthResult(
        String token,
        UserResponse user
) {
}
