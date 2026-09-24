package com.opt.backend.service.impl;

import com.opt.backend.entity.UserStats;
import com.opt.backend.repository.UserStatsRepository;
import com.opt.backend.service.UserStatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserStatsServiceImpl implements UserStatsService {

    private final UserStatsRepository userStatsRepository;

    public UserStatsServiceImpl(UserStatsRepository userStatsRepository) {
        this.userStatsRepository = userStatsRepository;
    }

    @Override
    @Transactional
    public UserStats getOrCreate(UUID userId) {
        return userStatsRepository.findById(userId).orElseGet(() -> userStatsRepository.save(new UserStats(userId)));
    }

    @Override
    @Transactional
    public void recordPlacement(UUID userId, int cellCount) {
        UserStats stats = getOrCreate(userId);
        stats.setPixelsPlaced(stats.getPixelsPlaced() + cellCount);
        userStatsRepository.save(stats);
    }

    @Override
    @Transactional
    public void recordCapture(UUID userId, int cellCount) {
        if (cellCount <= 0) return;
        UserStats stats = getOrCreate(userId);
        stats.setCapturesMade(stats.getCapturesMade() + cellCount);
        userStatsRepository.save(stats);
    }

    @Override
    @Transactional
    public void recordCombo(UUID userId) {
        UserStats stats = getOrCreate(userId);
        stats.setCombosTriggered(stats.getCombosTriggered() + 1);
        userStatsRepository.save(stats);
    }

    @Override
    @Transactional
    public void recordBombUse(UUID userId) {
        UserStats stats = getOrCreate(userId);
        stats.setBombsUsed(stats.getBombsUsed() + 1);
        userStatsRepository.save(stats);
    }

    @Override
    @Transactional
    public void recordTeamPoolContribution(UUID userId) {
        UserStats stats = getOrCreate(userId);
        stats.setTeamPoolContributions(stats.getTeamPoolContributions() + 1);
        userStatsRepository.save(stats);
    }
}
