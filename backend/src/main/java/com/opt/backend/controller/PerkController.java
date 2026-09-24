package com.opt.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opt.backend.common.security.UserPrincipal;
import com.opt.backend.dto.PerkCatalogResponse;
import com.opt.backend.dto.PerkPurchaseResponse;
import com.opt.backend.entity.PerkType;
import com.opt.backend.service.PerkService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perks")
public class PerkController {

    private final PerkService perkService;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;

    public PerkController(PerkService perkService, ObjectMapper objectMapper, PasswordEncoder passwordEncoder) {
        this.perkService = perkService;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<PerkCatalogResponse> getCatalog(@AuthenticationPrincipal UserPrincipal principal) {
        PerkCatalogResponse catalog = perkService.getCatalog(principal.getId());
        return ResponseEntity.ok().eTag(catalogETag(catalog)).body(catalog);
    }

    private String catalogETag(PerkCatalogResponse catalog) {
        try {
            String json = objectMapper.writeValueAsString(catalog);
            return passwordEncoder.encode(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @PostMapping("/{type}/purchase")
    public ResponseEntity<PerkPurchaseResponse> purchase(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable PerkType type) {
        return ResponseEntity.ok(perkService.purchase(principal.getId(), type));
    }
}
