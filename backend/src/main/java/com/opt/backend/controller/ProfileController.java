package com.opt.backend.controller;

import com.opt.backend.common.security.UserPrincipal;
import com.opt.backend.dto.OwnedPerkDto;
import com.opt.backend.dto.PerkDefinition;
import com.opt.backend.dto.ProfileResponse;
import com.opt.backend.dto.UnlockedAchievementDto;
import com.opt.backend.dto.UserStatsDto;
import com.opt.backend.entity.SkinId;
import com.opt.backend.entity.User;
import com.opt.backend.entity.UserStats;
import com.opt.backend.repository.UserPerkStatsRepository;
import com.opt.backend.repository.UserRepository;
import com.opt.backend.repository.UserSkinRepository;
import com.opt.backend.service.AchievementCatalog;
import com.opt.backend.service.AchievementService;
import com.opt.backend.service.UserStatsService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final UserStatsService userStatsService;
    private final UserPerkStatsRepository userPerkStatsRepository;
    private final UserSkinRepository userSkinRepository;
    private final AchievementService achievementService;
    private final AchievementCatalog achievementCatalog;

    public ProfileController(UserRepository userRepository, UserStatsService userStatsService,
                              UserPerkStatsRepository userPerkStatsRepository, UserSkinRepository userSkinRepository,
                              AchievementService achievementService, AchievementCatalog achievementCatalog) {
        this.userRepository = userRepository;
        this.userStatsService = userStatsService;
        this.userPerkStatsRepository = userPerkStatsRepository;
        this.userSkinRepository = userSkinRepository;
        this.achievementService = achievementService;
        this.achievementCatalog = achievementCatalog;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("Aucun utilisateur pour l'id " + principal.getId()));
        UserStats stats = userStatsService.getOrCreate(principal.getId());

        var ownedPerks = userPerkStatsRepository.findByIdUserId(principal.getId()).stream()
                .map(s -> new OwnedPerkDto(s.getId().getPerkType(), PerkDefinition.tierFor(s.getPurchaseCount()), s.getPurchaseCount()))
                .toList();

        var ownedSkins = java.util.Arrays.stream(SkinId.values())
                .filter(skinId -> userSkinRepository.existsByIdUserIdAndIdSkinId(principal.getId(), skinId))
                .toList();

        var achievements = achievementService.getUnlockedEntities(principal.getId()).stream()
                .map(unlocked -> {
                    var definition = achievementCatalog.get(unlocked.getId().getAchievementId());
                    return new UnlockedAchievementDto(definition.id(), definition.name(), definition.description(), unlocked.getUnlockedAt());
                })
                .toList();

        return ResponseEntity.ok(new ProfileResponse(
                user.getFullName(),
                user.getTeam(),
                user.getCreatedAt(),
                UserStatsDto.from(stats),
                ownedPerks,
                ownedSkins,
                achievements
        ));
    }
}
