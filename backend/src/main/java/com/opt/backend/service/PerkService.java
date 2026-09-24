package com.opt.backend.service;

import com.opt.backend.dto.PerkCatalogResponse;
import com.opt.backend.dto.PerkPurchaseResponse;
import com.opt.backend.entity.PerkType;

import java.util.UUID;

public interface PerkService {

    PerkCatalogResponse getCatalog(UUID userId);

    PerkPurchaseResponse purchase(UUID userId, PerkType type);
}
