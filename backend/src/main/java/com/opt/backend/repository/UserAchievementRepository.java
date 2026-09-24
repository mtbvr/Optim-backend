package com.opt.backend.repository;

import com.opt.backend.entity.AchievementId;
import com.opt.backend.entity.UserAchievement;
import com.opt.backend.entity.UserAchievementId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UserAchievementId> {

    List<UserAchievement> findByIdUserId(UUID userId);

    boolean existsByIdUserIdAndIdAchievementId(UUID userId, AchievementId achievementId);
}
