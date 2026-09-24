package com.opt.backend.dto;

import com.opt.backend.entity.PerkType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PlacePixelRequest(
        @NotNull @PositiveOrZero Integer x,
        @NotNull @PositiveOrZero Integer y,
        PerkType usePerk
) {
}
