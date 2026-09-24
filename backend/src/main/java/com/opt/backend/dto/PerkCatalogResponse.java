package com.opt.backend.dto;

import java.util.List;

public record PerkCatalogResponse(List<PerkDefinition> perks, PlayerStateDto me) {
}
