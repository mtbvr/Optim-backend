package com.opt.backend.service;

import com.opt.backend.entity.UserStats;

import java.util.UUID;

public interface UserStatsService {

    UserStats getOrCreate(UUID userId);

    void recordPlacement(UUID userId, int cellCount);

    void recordCapture(UUID userId, int cellCount);

    void recordCombo(UUID userId);

    void recordBombUse(UUID userId);

    void recordTeamPoolContribution(UUID userId);
}
