package com.opt.backend.service;

import com.opt.backend.dto.AchievementDefinition;
import com.opt.backend.entity.UserAchievement;

import java.util.List;
import java.util.UUID;

public interface AchievementService {

    List<AchievementDefinition> evaluateAndUnlock(UUID userId);

    List<UserAchievement> getUnlockedEntities(UUID userId);
}
