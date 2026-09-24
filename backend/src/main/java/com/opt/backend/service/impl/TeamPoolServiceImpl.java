package com.opt.backend.service.impl;

import com.opt.backend.common.exception.InsufficientPointsException;
import com.opt.backend.common.exception.InvalidContributionException;
import com.opt.backend.common.pixelwars.TeamPoolTracker;
import com.opt.backend.dto.AchievementDefinition;
import com.opt.backend.dto.ContributeResponse;
import com.opt.backend.dto.PlayerStateDto;
import com.opt.backend.entity.User;
import com.opt.backend.repository.UserRepository;
import com.opt.backend.service.AchievementService;
import com.opt.backend.service.TeamPoolService;
import com.opt.backend.service.UserStatsService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TeamPoolServiceImpl implements TeamPoolService {

    private final UserRepository userRepository;
    private final TeamPoolTracker teamPoolTracker;
    private final UserStatsService userStatsService;
    private final AchievementService achievementService;

    public TeamPoolServiceImpl(UserRepository userRepository, TeamPoolTracker teamPoolTracker,
                                UserStatsService userStatsService, AchievementService achievementService) {
        this.userRepository = userRepository;
        this.teamPoolTracker = teamPoolTracker;
        this.userStatsService = userStatsService;
        this.achievementService = achievementService;
    }

    @Override
    @Transactional
    public ContributeResponse contribute(UUID userId, int amount) {
        if (amount <= 0) {
            throw new InvalidContributionException("INVALID_AMOUNT", "Le montant doit etre positif");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Aucun utilisateur pour l'id " + userId));
        if (user.getPoints() < amount) {
            throw new InsufficientPointsException(amount, user.getPoints());
        }

        user.setPoints(user.getPoints() - amount);
        userRepository.save(user);

        teamPoolTracker.contribute(user.getTeam(), amount);
        userStatsService.recordTeamPoolContribution(userId);

        List<AchievementDefinition> newAchievements = achievementService.evaluateAndUnlock(userId);
        return new ContributeResponse(PlayerStateDto.from(user), teamPoolTracker.snapshot(), newAchievements);
    }
}
