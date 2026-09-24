package com.opt.backend.dto;

import com.opt.backend.entity.Pixel;
import com.opt.backend.entity.Team;

public record PixelDto(int x, int y, Team team) {

    public static PixelDto from(Pixel pixel) {
        return new PixelDto(pixel.getId().getX(), pixel.getId().getY(), pixel.getTeam());
    }
}
