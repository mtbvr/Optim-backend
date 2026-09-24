package com.opt.backend.dto;

import com.opt.backend.entity.PerkType;

public record OwnedPerkDto(PerkType type, int tier, int purchaseCount) {
}
