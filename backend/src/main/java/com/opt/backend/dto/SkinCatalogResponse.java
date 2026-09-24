package com.opt.backend.dto;

import com.opt.backend.entity.SkinId;

import java.util.List;

public record SkinCatalogResponse(List<SkinDefinition> skins, List<SkinId> owned, SkinId selected, PlayerStateDto me) {
}
