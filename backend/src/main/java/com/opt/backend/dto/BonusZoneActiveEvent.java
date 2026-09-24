package com.opt.backend.dto;

public record BonusZoneActiveEvent(String type, int x, int y, long until) {

    public static BonusZoneActiveEvent of(BonusZoneDto zone) {
        return new BonusZoneActiveEvent("BONUS_ZONE_ACTIVE", zone.x(), zone.y(), zone.until());
    }
}
