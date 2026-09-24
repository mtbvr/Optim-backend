package com.opt.backend.service;

import com.opt.backend.dto.PlayerStateDto;
import com.opt.backend.dto.SkinCatalogResponse;
import com.opt.backend.dto.SkinPurchaseResponse;
import com.opt.backend.entity.SkinId;

import java.util.UUID;

public interface SkinService {

    SkinCatalogResponse getCatalog(UUID userId);

    SkinPurchaseResponse purchase(UUID userId, SkinId skinId);

    PlayerStateDto select(UUID userId, SkinId skinId);
}
