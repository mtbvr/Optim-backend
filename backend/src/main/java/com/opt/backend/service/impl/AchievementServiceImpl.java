package com.opt.backend.service.impl;

import com.opt.backend.dto.AchievementDefinition;
import com.opt.backend.entity.AchievementId;
import com.opt.backend.entity.PerkType;
import com.opt.backend.entity.SkinId;
import com.opt.backend.entity.UserAchievement;
import com.opt.backend.entity.UserAchievementId;
import com.opt.backend.entity.UserPerkStats;
import com.opt.backend.entity.UserSkin;
import com.opt.backend.entity.UserSkinId;
import com.opt.backend.entity.UserStats;
import com.opt.backend.repository.UserAchievementRepository;
import com.opt.backend.repository.UserPerkStatsRepository;
import com.opt.backend.repository.UserSkinRepository;
import com.opt.backend.service.AchievementCatalog;
import com.opt.backend.service.AchievementService;
import com.opt.backend.service.SkinCatalog;
import com.opt.backend.service.UserStatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AchievementServiceImpl implements AchievementService {

    private final UserStatsService userStatsService;
    private final UserPerkStatsRepository userPerkStatsRepository;
    private final UserSkinRepository userSkinRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementCatalog achievementCatalog;
    private final SkinCatalog skinCatalog;

    public AchievementServiceImpl(UserStatsService userStatsService, UserPerkStatsRepository userPerkStatsRepository,
                                   UserSkinRepository userSkinRepository, UserAchievementRepository userAchievementRepository,
                                   AchievementCatalog achievementCatalog, SkinCatalog skinCatalog) {
        this.userStatsService = userStatsService;
        this.userPerkStatsRepository = userPerkStatsRepository;
        this.userSkinRepository = userSkinRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.achievementCatalog = achievementCatalog;
        this.skinCatalog = skinCatalog;
    }

    @Override
    @Transactional
    public List<AchievementDefinition> evaluateAndUnlock(UUID userId) {
        UserStats stats = userStatsService.getOrCreate(userId);
        List<UserPerkStats> perkStats = userPerkStatsRepository.findByIdUserId(userId);
        List<SkinId> ownedSkins = userSkinRepository.findByIdUserId(userId).stream()
                .map(userSkin -> userSkin.getId().getSkinId())
                .toList();

        List<AchievementDefinition> unlocked = new ArrayList<>();
        for (AchievementDefinition definition : achievementCatalog.all()) {
            if (userAchievementRepository.existsByIdUserIdAndIdAchievementId(userId, definition.id())) {
                continue;
            }
            if (isConditionMet(definition.id(), stats, perkStats, ownedSkins)) {
                userAchievementRepository.save(new UserAchievement(new UserAchievementId(userId, definition.id())));
                grantSkinIfAny(userId, definition.id());
                unlocked.add(definition);
            }
        }
        return unlocked;
    }

    @Override
    public List<UserAchievement> getUnlockedEntities(UUID userId) {
        return userAchievementRepository.findByIdUserId(userId);
    }

    private boolean isConditionMet(AchievementId id, UserStats stats, List<UserPerkStats> perkStats, List<SkinId> ownedSkins) {
        return switch (id) {
            case PREMIER_PIXEL -> stats.getPixelsPlaced() >= 1;
            case CENTURION -> stats.getPixelsPlaced() >= 100;
            case CONQUERANT -> stats.getCapturesMade() >= 10;
            case COMBO_MASTER -> stats.getCombosTriggered() >= 5;
            case ARTIFICIER -> stats.getBombsUsed() >= 10;
            case STRATEGE -> perkStats.size() >= PerkType.values().length;
            case COLLECTIONNEUR -> ownedSkins.size() >= skinCatalog.all().size();
            case VETERAN_EQUIPE -> stats.getTeamPoolContributions() >= 3;
        };
    }

    private void grantSkinIfAny(UUID userId, AchievementId id) {
        SkinId skinId = switch (id) {
            case CENTURION -> SkinId.BORDER_DASHED;
            case CONQUERANT -> SkinId.BORDER_THICK;
            case STRATEGE -> SkinId.CURSOR_TARGET;
            default -> null;
        };
        if (skinId != null && !userSkinRepository.existsByIdUserIdAndIdSkinId(userId, skinId)) {
            userSkinRepository.save(new UserSkin(new UserSkinId(userId, skinId)));
        }
    }
}
