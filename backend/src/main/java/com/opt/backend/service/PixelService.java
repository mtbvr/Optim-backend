package com.opt.backend.service;

import com.opt.backend.dto.BoardResponse;
import com.opt.backend.dto.PlacePixelRequest;
import com.opt.backend.dto.PlacePixelResponse;

import java.util.UUID;

public interface PixelService {

    BoardResponse getBoard(UUID userId);

    PlacePixelResponse placePixel(UUID userId, PlacePixelRequest request);
}
