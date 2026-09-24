package com.opt.backend.service.impl;

import com.opt.backend.common.exception.InsufficientPointsException;
import com.opt.backend.common.exception.SkinAlreadyOwnedException;
import com.opt.backend.common.exception.SkinNotOwnedException;
import com.opt.backend.common.exception.SkinNotPurchasableException;
import com.opt.backend.dto.AchievementDefinition;
import com.opt.backend.dto.PlayerStateDto;
import com.opt.backend.dto.SkinCatalogResponse;
import com.opt.backend.dto.SkinDefinition;
import com.opt.backend.dto.SkinPurchaseResponse;
import com.opt.backend.entity.SkinId;
import com.opt.backend.entity.User;
import com.opt.backend.entity.UserSkin;
import com.opt.backend.entity.UserSkinId;
import com.opt.backend.repository.UserRepository;
import com.opt.backend.repository.UserSkinRepository;
import com.opt.backend.service.AchievementService;
import com.opt.backend.service.SkinCatalog;
import com.opt.backend.service.SkinService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SkinServiceImpl implements SkinService {

    private final UserRepository userRepository;
    private final UserSkinRepository userSkinRepository;
    private final SkinCatalog skinCatalog;
    private final AchievementService achievementService;

    public SkinServiceImpl(UserRepository userRepository, UserSkinRepository userSkinRepository,
                            SkinCatalog skinCatalog, AchievementService achievementService) {
        this.userRepository = userRepository;
        this.userSkinRepository = userSkinRepository;
        this.skinCatalog = skinCatalog;
        this.achievementService = achievementService;
    }

    @Override
    public SkinCatalogResponse getCatalog(UUID userId) {
        User user = getUser(userId);
        List<SkinId> owned = userSkinRepository.findByIdUserId(userId).stream()
                .map(userSkin -> userSkin.getId().getSkinId())
                .toList();
        SkinId selected = user.getSelectedSkin() != null ? SkinId.valueOf(user.getSelectedSkin()) : null;
        return new SkinCatalogResponse(skinCatalog.all(), owned, selected, PlayerStateDto.from(user));
    }

    @Override
    @Transactional
    public SkinPurchaseResponse purchase(UUID userId, SkinId skinId) {
        User user = getUser(userId);
        if (userSkinRepository.existsByIdUserIdAndIdSkinId(userId, skinId)) {
            throw new SkinAlreadyOwnedException();
        }
        SkinDefinition definition = skinCatalog.get(skinId);
        if (definition.requiresAchievement() != null) {
            throw new SkinNotPurchasableException();
        }
        if (user.getPoints() < definition.cost()) {
            throw new InsufficientPointsException(definition.cost(), user.getPoints());
        }

        user.setPoints(user.getPoints() - definition.cost());
        user.setSelectedSkin(skinId.name());
        userRepository.save(user);
        userSkinRepository.save(new UserSkin(new UserSkinId(userId, skinId)));

        List<AchievementDefinition> newAchievements = achievementService.evaluateAndUnlock(userId);
        return new SkinPurchaseResponse(PlayerStateDto.from(user), newAchievements);
    }

    @Override
    @Transactional
    public PlayerStateDto select(UUID userId, SkinId skinId) {
        User user = getUser(userId);
        if (skinId != null && !userSkinRepository.existsByIdUserIdAndIdSkinId(userId, skinId)) {
            throw new SkinNotOwnedException();
        }
        user.setSelectedSkin(skinId != null ? skinId.name() : null);
        userRepository.save(user);

        return PlayerStateDto.from(user);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Aucun utilisateur pour l'id " + userId));
    }
}
