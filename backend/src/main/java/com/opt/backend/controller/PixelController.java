package com.opt.backend.controller;

import com.opt.backend.common.security.UserPrincipal;
import com.opt.backend.dto.BoardResponse;
import com.opt.backend.dto.PlacePixelRequest;
import com.opt.backend.dto.PlacePixelResponse;
import com.opt.backend.service.PixelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pixels")
public class PixelController {

    private final PixelService pixelService;

    public PixelController(PixelService pixelService) {
        this.pixelService = pixelService;
    }

    @GetMapping
    public ResponseEntity<BoardResponse> getBoard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(pixelService.getBoard(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<PlacePixelResponse> placePixel(@AuthenticationPrincipal UserPrincipal principal,
                                                           @Valid @RequestBody PlacePixelRequest request) {
        return ResponseEntity.ok(pixelService.placePixel(principal.getId(), request));
    }
}
