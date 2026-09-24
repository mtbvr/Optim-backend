package com.opt.backend.dto;

public record BonusZoneClearedEvent(String type) {

    public static BonusZoneClearedEvent of() {
        return new BonusZoneClearedEvent("BONUS_ZONE_CLEARED");
    }
}
