package com.opt.backend.service.impl;

import com.opt.backend.common.config.PixelWarsProperties;
import com.opt.backend.common.exception.CooldownActiveException;
import com.opt.backend.common.exception.InvalidPixelRequestException;
import com.opt.backend.common.exception.NoBombChargeException;
import com.opt.backend.common.exception.NoFortressChargeException;
import com.opt.backend.common.pixelwars.BoardGrid;
import com.opt.backend.common.pixelwars.BonusZoneTracker;
import com.opt.backend.common.pixelwars.CaptureEvaluator;
import com.opt.backend.common.pixelwars.ComboEvaluator;
import com.opt.backend.common.pixelwars.Coord;
import com.opt.backend.common.pixelwars.GlobalPlacementCounter;
import com.opt.backend.common.pixelwars.LeaderboardTracker;
import com.opt.backend.common.pixelwars.PerkEffectsTracker;
import com.opt.backend.common.pixelwars.TeamPoolTracker;
import com.opt.backend.common.websocket.PixelWebSocketHandler;
import com.opt.backend.dto.AchievementDefinition;
import com.opt.backend.dto.BoardResponse;
import com.opt.backend.dto.LeaderboardUpdateEvent;
import com.opt.backend.dto.PixelDto;
import com.opt.backend.dto.PixelsCapturedEvent;
import com.opt.backend.dto.PerkDefinition;
import com.opt.backend.dto.PixelsPlacedEvent;
import com.opt.backend.dto.PlacePixelRequest;
import com.opt.backend.dto.PlacePixelResponse;
import com.opt.backend.dto.PlayerStateDto;
import com.opt.backend.entity.PerkType;
import com.opt.backend.entity.Pixel;
import com.opt.backend.entity.PixelId;
import com.opt.backend.entity.Team;
import com.opt.backend.entity.User;
import com.opt.backend.repository.PixelRepository;
import com.opt.backend.repository.UserPerkStatsRepository;
import com.opt.backend.repository.UserRepository;
import com.opt.backend.service.AchievementService;
import com.opt.backend.service.PixelService;
import com.opt.backend.service.UserStatsService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PixelServiceImpl implements PixelService {

    private static final Duration FORTRESS_BASE_DURATION = Duration.ofSeconds(60);
    private static final Duration FORTRESS_DURATION_PER_TIER = Duration.ofSeconds(15);

    private final PixelRepository pixelRepository;
    private final UserRepository userRepository;
    private final UserPerkStatsRepository userPerkStatsRepository;
    private final PixelWarsProperties properties;
    private final PixelWebSocketHandler webSocketHandler;
    private final BoardGrid boardGrid;
    private final ComboEvaluator comboEvaluator;
    private final CaptureEvaluator captureEvaluator;
    private final LeaderboardTracker leaderboardTracker;
    private final PerkEffectsTracker perkEffectsTracker;
    private final BonusZoneTracker bonusZoneTracker;
    private final TeamPoolTracker teamPoolTracker;
    private final UserStatsService userStatsService;
    private final AchievementService achievementService;
    private final GlobalPlacementCounter globalPlacementCounter;

    private final Map<UUID, Instant> lastPlacementByUser = new ConcurrentHashMap<>();

    public PixelServiceImpl(PixelRepository pixelRepository, UserRepository userRepository,
                             UserPerkStatsRepository userPerkStatsRepository, PixelWarsProperties properties,
                             PixelWebSocketHandler webSocketHandler, BoardGrid boardGrid, ComboEvaluator comboEvaluator,
                             CaptureEvaluator captureEvaluator, LeaderboardTracker leaderboardTracker,
                             PerkEffectsTracker perkEffectsTracker, BonusZoneTracker bonusZoneTracker,
                             TeamPoolTracker teamPoolTracker, UserStatsService userStatsService,
                             AchievementService achievementService, GlobalPlacementCounter globalPlacementCounter) {
        this.pixelRepository = pixelRepository;
        this.userRepository = userRepository;
        this.userPerkStatsRepository = userPerkStatsRepository;
        this.properties = properties;
        this.webSocketHandler = webSocketHandler;
        this.boardGrid = boardGrid;
        this.comboEvaluator = comboEvaluator;
        this.captureEvaluator = captureEvaluator;
        this.leaderboardTracker = leaderboardTracker;
        this.perkEffectsTracker = perkEffectsTracker;
        this.bonusZoneTracker = bonusZoneTracker;
        this.teamPoolTracker = teamPoolTracker;
        this.userStatsService = userStatsService;
        this.achievementService = achievementService;
        this.globalPlacementCounter = globalPlacementCounter;
    }

    @Override
    public BoardResponse getBoard(UUID userId) {
        List<PixelDto> pixels = boardGrid.snapshot();

        return new BoardResponse(
                properties.getWidth(),
                properties.getHeight(),
                properties.getCooldownSeconds(),
                teamColors(),
                pixels,
                leaderboardTracker.snapshot(),
                PlayerStateDto.from(getUser(userId)),
                bonusZoneTracker.getActiveZone(),
                teamPoolTracker.snapshot()
        );
    }

    @Override
    @Transactional
    public PlacePixelResponse placePixel(UUID userId, PlacePixelRequest request) {
        User user = getUser(userId);
        Team team = user.getTeam();
        String color = colorOf(team);

        validateCoordinates(request.x(), request.y());
        validateUsePerk(request.usePerk());
        checkCooldown(userId, team);

        boolean isBomb = request.usePerk() == PerkType.BOMB;
        boolean isFortress = request.usePerk() == PerkType.FORTRESS;
        if (isBomb) {
            if (user.getBombCharges() <= 0) {
                throw new NoBombChargeException();
            }
            user.setBombCharges(user.getBombCharges() - 1);
        }
        if (isFortress) {
            if (user.getFortressCharges() <= 0) {
                throw new NoFortressChargeException();
            }
            user.setFortressCharges(user.getFortressCharges() - 1);
        }

        List<Coord> targetCells = resolveTargetCells(request.x(), request.y(), isBomb);
        PlacementResult result = applyPlacement(userId, team, color, targetCells, isBomb, isFortress,
                request.x(), request.y());
        for (CellChange change : result.changes()) {
            persistCell(change, color, userId);
        }

        boolean bonusApplied = bonusZoneTracker.isActiveAt(request.x(), request.y());
        int pointsAwarded = result.basePoints() + result.comboPoints() + result.capturePoints();
        if (bonusApplied) {
            pointsAwarded *= properties.getBonusZoneMultiplier();
        }

        lastPlacementByUser.put(userId, Instant.now());
        perkEffectsTracker.consumeSpeedBuffCharge(userId);
        perkEffectsTracker.consumeComboBuffCharge(userId);
        user.setPoints(user.getPoints() + pointsAwarded);
        userRepository.save(user);

        userStatsService.recordPlacement(userId, targetCells.size());
        if (!result.capturedDtos().isEmpty()) {
            userStatsService.recordCapture(userId, result.capturedDtos().size());
        }
        if (result.comboPoints() > 0) {
            userStatsService.recordCombo(userId);
        }
        if (isBomb) {
            userStatsService.recordBombUse(userId);
        }
        List<AchievementDefinition> newAchievements = achievementService.evaluateAndUnlock(userId);

        webSocketHandler.broadcast(PixelsPlacedEvent.of(result.placedDtos(), globalPlacementCounter.incrementAndGet()));
        if (!result.capturedDtos().isEmpty()) {
            webSocketHandler.broadcast(PixelsCapturedEvent.of(result.capturedDtos()));
        }
        webSocketHandler.broadcast(LeaderboardUpdateEvent.of(leaderboardTracker.snapshot()));

        int nextCooldownSeconds = effectiveCooldownSeconds(userId, team);
        return new PlacePixelResponse(result.placedDtos(), result.capturedDtos(), pointsAwarded,
                result.comboPoints(), result.capturePoints(), bonusApplied, PlayerStateDto.from(user),
                nextCooldownSeconds, newAchievements);
    }

    private synchronized PlacementResult applyPlacement(UUID userId, Team team, String color,
                                                          List<Coord> targetCells, boolean isBomb,
                                                          boolean isFortress, int x, int y) {
        List<PixelDto> placedDtos = new ArrayList<>();
        List<CellChange> changes = new ArrayList<>();
        for (Coord cell : targetCells) {
            changes.add(applyCellMutation(cell, team));
            placedDtos.add(new PixelDto(cell.x(), cell.y(), team));
        }
        if (isFortress) {
            perkEffectsTracker.fortifyCell(new Coord(x, y), fortressDuration(userId));
        }

        int basePoints = properties.getPlacementPoints();
        int comboPoints = isBomb
                ? 0
                : comboEvaluator.evaluate(boardGrid, x, y, team) * perkEffectsTracker.peekComboMultiplier(userId);

        int capturePoints = 0;
        List<PixelDto> capturedDtos = new ArrayList<>();
        if (!perkEffectsTracker.isShieldActive(team.opposite())) {
            List<Coord> capturedCells = captureEvaluator.evaluate(boardGrid, targetCells, team).stream()
                    .filter(coord -> !perkEffectsTracker.isCellFortified(coord))
                    .toList();
            for (Coord cell : capturedCells) {
                changes.add(applyCellMutation(cell, team));
                capturedDtos.add(new PixelDto(cell.x(), cell.y(), team));
            }
            capturePoints = capturedCells.size() * properties.getCaptureBonusPerPixel();
        }

        return new PlacementResult(placedDtos, capturedDtos, basePoints, comboPoints, capturePoints, changes);
    }

    private record PlacementResult(List<PixelDto> placedDtos, List<PixelDto> capturedDtos, int basePoints,
                                    int comboPoints, int capturePoints, List<CellChange> changes) {
    }

    private record CellChange(Coord cell, Team previousTeam, Team newTeam) {
    }

    private CellChange applyCellMutation(Coord cell, Team newTeam) {
        Team previousTeam = boardGrid.get(cell.x(), cell.y());
        boardGrid.set(cell.x(), cell.y(), newTeam);
        return new CellChange(cell, previousTeam, newTeam);
    }

    private void persistCell(CellChange change, String color, UUID userId) {
        PixelId id = new PixelId(change.cell().x(), change.cell().y());
        Optional<Pixel> existing = pixelRepository.findById(id);
        UUID previousOwner = existing.map(Pixel::getUpdatedBy).orElse(null);

        Pixel pixel = existing.orElseGet(() -> new Pixel(id, color, change.newTeam(), userId));
        pixel.setColor(color);
        pixel.setTeam(change.newTeam());
        pixel.setUpdatedBy(userId);
        pixelRepository.save(pixel);

        leaderboardTracker.recordChange(previousOwner, change.previousTeam(), userId, change.newTeam());
    }

    private List<Coord> resolveTargetCells(int x, int y, boolean isBomb) {
        if (!isBomb) {
            return List.of(new Coord(x, y));
        }
        List<Coord> cells = new ArrayList<>(9);
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int cx = x + dx;
                int cy = y + dy;
                if (boardGrid.isInBounds(cx, cy)) {
                    cells.add(new Coord(cx, cy));
                }
            }
        }
        return cells;
    }

    private Duration fortressDuration(UUID userId) {
        int purchaseCount = userPerkStatsRepository.findByIdUserIdAndIdPerkType(userId, PerkType.FORTRESS)
                .map(stats -> stats.getPurchaseCount())
                .orElse(0);
        int tier = PerkDefinition.tierFor(purchaseCount);
        return FORTRESS_BASE_DURATION.plus(FORTRESS_DURATION_PER_TIER.multipliedBy(tier));
    }

    private Map<Team, String> teamColors() {
        return Map.of(Team.RED, properties.getRedColor(), Team.BLUE, properties.getBlueColor());
    }

    private String colorOf(Team team) {
        return team == Team.RED ? properties.getRedColor() : properties.getBlueColor();
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Aucun utilisateur pour l'id " + userId));
    }

    private void validateCoordinates(int x, int y) {
        if (x < 0 || x >= properties.getWidth() || y < 0 || y >= properties.getHeight()) {
            throw new InvalidPixelRequestException("OUT_OF_BOUNDS", "Coordonnees hors de la grille");
        }
    }

    private void validateUsePerk(PerkType usePerk) {
        if (usePerk != null && usePerk != PerkType.BOMB && usePerk != PerkType.FORTRESS) {
            throw new InvalidPixelRequestException("PERK_NOT_PLACEABLE",
                    "Ce perk ne s'utilise pas via un placement, achete-le pour l'activer directement");
        }
    }

    private void checkCooldown(UUID userId, Team team) {
        double multiplier = perkEffectsTracker.peekSpeedMultiplier(userId, team);
        if (multiplier <= 0.0) {
            return;
        }
        Instant lastPlacement = lastPlacementByUser.get(userId);
        if (lastPlacement == null) {
            return;
        }
        long elapsedSeconds = Instant.now().getEpochSecond() - lastPlacement.getEpochSecond();
        long effectiveCooldown = Math.round(properties.getCooldownSeconds() * multiplier);
        long remaining = effectiveCooldown - elapsedSeconds;
        if (remaining > 0) {
            throw new CooldownActiveException(remaining);
        }
    }

    private int effectiveCooldownSeconds(UUID userId, Team team) {
        double multiplier = perkEffectsTracker.peekSpeedMultiplier(userId, team);
        return (int) Math.round(properties.getCooldownSeconds() * multiplier);
    }
}
