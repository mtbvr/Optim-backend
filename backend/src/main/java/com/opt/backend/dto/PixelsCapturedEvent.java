package com.opt.backend.dto;

import java.util.List;

public record PixelsCapturedEvent(String type, List<PixelDto> cells) {

    public static PixelsCapturedEvent of(List<PixelDto> cells) {
        return new PixelsCapturedEvent("PIXELS_CAPTURED", cells);
    }
}
