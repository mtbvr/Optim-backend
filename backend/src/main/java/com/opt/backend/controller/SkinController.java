package com.opt.backend.controller;

import com.opt.backend.common.security.UserPrincipal;
import com.opt.backend.dto.PlayerStateDto;
import com.opt.backend.dto.SelectSkinRequest;
import com.opt.backend.dto.SkinCatalogResponse;
import com.opt.backend.dto.SkinPurchaseResponse;
import com.opt.backend.entity.SkinId;
import com.opt.backend.service.SkinService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skins")
public class SkinController {

    private final SkinService skinService;

    public SkinController(SkinService skinService) {
        this.skinService = skinService;
    }

    @GetMapping
    public ResponseEntity<SkinCatalogResponse> getCatalog(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(skinService.getCatalog(principal.getId()));
    }

    @PostMapping("/{skinId}/purchase")
    public ResponseEntity<SkinPurchaseResponse> purchase(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable SkinId skinId) {
        return ResponseEntity.ok(skinService.purchase(principal.getId(), skinId));
    }

    @PostMapping("/select")
    public ResponseEntity<PlayerStateDto> select(@AuthenticationPrincipal UserPrincipal principal,
                                                   @RequestBody SelectSkinRequest request) {
        return ResponseEntity.ok(skinService.select(principal.getId(), request.skinId()));
    }
}
