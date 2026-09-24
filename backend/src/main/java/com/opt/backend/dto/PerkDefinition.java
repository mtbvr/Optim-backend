package com.opt.backend.dto;

import com.opt.backend.entity.PerkType;

public record PerkDefinition(PerkType type, String name, String description, int cost, int tier, int purchaseCount) {

    private static final int MAX_TIER = 3;
    private static final int PURCHASES_PER_TIER = 5;

    public static int tierFor(int purchaseCount) {
        return Math.min(MAX_TIER, purchaseCount / PURCHASES_PER_TIER);
    }
}
