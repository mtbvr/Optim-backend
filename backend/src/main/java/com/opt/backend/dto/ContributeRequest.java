package com.opt.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ContributeRequest(
        @NotNull @Positive Integer amount
) {
}
