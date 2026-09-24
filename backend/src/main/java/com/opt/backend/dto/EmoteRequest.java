package com.opt.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record EmoteRequest(
        @NotBlank String emoji
) {
}
