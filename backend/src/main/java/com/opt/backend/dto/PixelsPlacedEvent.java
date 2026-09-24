package com.opt.backend.dto;

import java.util.List;

public record PixelsPlacedEvent(String type, List<PixelDto> cells, long totalPlacements) {

    public static PixelsPlacedEvent of(List<PixelDto> cells, long totalPlacements) {
        return new PixelsPlacedEvent("PIXELS_PLACED", cells, totalPlacements);
    }
}
