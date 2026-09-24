package com.opt.backend.service.impl;

import com.opt.backend.common.exception.InsufficientPointsException;
import com.opt.backend.common.exception.SpeedBuffAlreadyActiveException;
import com.opt.backend.common.pixelwars.PerkEffectsTracker;
import com.opt.backend.dto.AchievementDefinition;
import com.opt.backend.dto.PerkCatalogResponse;
import com.opt.backend.dto.PerkDefinition;
import com.opt.backend.dto.PerkPurchaseResponse;
import com.opt.backend.dto.PlayerStateDto;
import com.opt.backend.entity.PerkType;
import com.opt.backend.entity.User;
import com.opt.backend.entity.UserPerkStats;
import com.opt.backend.entity.UserPerkStatsId;
import com.opt.backend.repository.UserPerkStatsRepository;
import com.opt.backend.repository.UserRepository;
import com.opt.backend.service.AchievementService;
import com.opt.backend.service.PerkCatalog;
import com.opt.backend.service.PerkService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PerkServiceImpl implements PerkService {

    private static final double CAFE_COOLDOWN_MULTIPLIER = 0.5;
    private static final int CAFE_BASE_USES = 3;
    private static final Duration SHIELD_BASE_DURATION = Duration.ofSeconds(20);
    private static final Duration SHIELD_DURATION_PER_TIER = Duration.ofSeconds(5);
    private static final int FRENZY_MULTIPLIER = 2;
    private static final int FRENZY_BASE_USES = 3;

    private final UserRepository userRepository;
    private final UserPerkStatsRepository userPerkStatsRepository;
    private final PerkCatalog perkCatalog;
    private final PerkEffectsTracker perkEffectsTracker;
    private final AchievementService achievementService;

    public PerkServiceImpl(UserRepository userRepository, UserPerkStatsRepository userPerkStatsRepository,
                            PerkCatalog perkCatalog, PerkEffectsTracker perkEffectsTracker,
                            AchievementService achievementService) {
        this.userRepository = userRepository;
        this.userPerkStatsRepository = userPerkStatsRepository;
        this.perkCatalog = perkCatalog;
        this.perkEffectsTracker = perkEffectsTracker;
        this.achievementService = achievementService;
    }

    @Override
    public PerkCatalogResponse getCatalog(UUID userId) {
        User user = getUser(userId);
        Map<PerkType, Integer> purchaseCounts = purchaseCountsByType(userId);

        List<PerkDefinition> personalized = perkCatalog.all().stream()
                .map(definition -> withProgress(definition, purchaseCounts.getOrDefault(definition.type(), 0)))
                .toList();

        return new PerkCatalogResponse(personalized, PlayerStateDto.from(user));
    }

    @Override
    @Transactional
    public PerkPurchaseResponse purchase(UUID userId, PerkType type) {
        User user = getUser(userId);
        PerkDefinition definition = perkCatalog.get(type);

        if ((type == PerkType.CAFE || type == PerkType.RAFALE) && perkEffectsTracker.isSpeedBuffActive(userId)) {
            throw new SpeedBuffAlreadyActiveException();
        }
        if (user.getPoints() < definition.cost()) {
            throw new InsufficientPointsException(definition.cost(), user.getPoints());
        }

        user.setPoints(user.getPoints() - definition.cost());

        UserPerkStatsId statsId = new UserPerkStatsId(userId, type);
        UserPerkStats stats = userPerkStatsRepository.findById(statsId).orElseGet(() -> new UserPerkStats(statsId));
        stats.setPurchaseCount(stats.getPurchaseCount() + 1);
        userPerkStatsRepository.save(stats);
        int tier = PerkDefinition.tierFor(stats.getPurchaseCount());

        switch (type) {
            case BOMB -> user.setBombCharges(user.getBombCharges() + 1);
            case FORTRESS -> user.setFortressCharges(user.getFortressCharges() + 1);
            case CAFE -> perkEffectsTracker.activateCafe(userId, CAFE_COOLDOWN_MULTIPLIER, CAFE_BASE_USES + tier);
            case RAFALE -> perkEffectsTracker.activateRafale(userId, 1 + tier);
            case SHIELD -> perkEffectsTracker.activateShield(user.getTeam(),
                    SHIELD_BASE_DURATION.plus(SHIELD_DURATION_PER_TIER.multipliedBy(tier)));
            case FRENZY -> perkEffectsTracker.activateFrenzy(userId, FRENZY_MULTIPLIER, FRENZY_BASE_USES + tier);
        }

        userRepository.save(user);

        List<AchievementDefinition> newAchievements = achievementService.evaluateAndUnlock(userId);
        return new PerkPurchaseResponse(PlayerStateDto.from(user), tier, newAchievements);
    }

    private Map<PerkType, Integer> purchaseCountsByType(UUID userId) {
        return userPerkStatsRepository.findByIdUserId(userId).stream()
                .collect(java.util.stream.Collectors.toMap(s -> s.getId().getPerkType(), UserPerkStats::getPurchaseCount));
    }

    private PerkDefinition withProgress(PerkDefinition base, int purchaseCount) {
        return new PerkDefinition(base.type(), base.name(), base.description(), base.cost(),
                PerkDefinition.tierFor(purchaseCount), purchaseCount);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Aucun utilisateur pour l'id " + userId));
    }
}
