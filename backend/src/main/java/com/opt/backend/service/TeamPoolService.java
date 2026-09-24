package com.opt.backend.service;

import com.opt.backend.dto.ContributeResponse;

import java.util.UUID;

public interface TeamPoolService {

    ContributeResponse contribute(UUID userId, int amount);
}
