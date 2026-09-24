package com.opt.backend.common.pixelwars;

import com.opt.backend.entity.Team;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PerkEffectsTracker {

    public static final double TEAM_POOL_SPEED_MULTIPLIER = 0.5;

    private record SpeedBuff(double multiplier, int usesRemaining) {
    }

    private record ComboBuff(int multiplier, int usesRemaining) {
    }

    private final Map<UUID, SpeedBuff> speedBuffs = new ConcurrentHashMap<>();
    private final Map<UUID, ComboBuff> comboBuffs = new ConcurrentHashMap<>();
    private final Map<Team, Instant> shieldActiveUntil = new ConcurrentHashMap<>();
    private final Map<Coord, Instant> fortifiedUntil = new ConcurrentHashMap<>();
    private final Map<Team, Instant> teamSpeedBuffUntil = new ConcurrentHashMap<>();

    public void activateCafe(UUID userId, double multiplier, int uses) {
        speedBuffs.put(userId, new SpeedBuff(multiplier, uses));
    }

    public void activateRafale(UUID userId, int uses) {
        speedBuffs.put(userId, new SpeedBuff(0.0, uses));
    }

    public boolean isSpeedBuffActive(UUID userId) {
        return speedBuffs.containsKey(userId);
    }

    public void activateShield(Team team, Duration duration) {
        shieldActiveUntil.put(team, Instant.now().plus(duration));
    }

    public boolean isShieldActive(Team team) {
        Instant until = shieldActiveUntil.get(team);
        return until != null && until.isAfter(Instant.now());
    }

    public void activateFrenzy(UUID userId, int multiplier, int uses) {
        comboBuffs.put(userId, new ComboBuff(multiplier, uses));
    }

    public int peekComboMultiplier(UUID userId) {
        ComboBuff buff = comboBuffs.get(userId);
        return buff == null ? 1 : buff.multiplier();
    }

    public void consumeComboBuffCharge(UUID userId) {
        comboBuffs.computeIfPresent(userId, (id, buff) -> {
            int remaining = buff.usesRemaining() - 1;
            return remaining > 0 ? new ComboBuff(buff.multiplier(), remaining) : null;
        });
    }

    public void fortifyCell(Coord coord, Duration duration) {
        fortifiedUntil.put(coord, Instant.now().plus(duration));
    }

    public boolean isCellFortified(Coord coord) {
        Instant until = fortifiedUntil.get(coord);
        return until != null && until.isAfter(Instant.now());
    }

    public void activateTeamSpeedBuff(Team team, Duration duration) {
        teamSpeedBuffUntil.put(team, Instant.now().plus(duration));
    }

    public boolean isTeamSpeedBuffActive(Team team) {
        Instant until = teamSpeedBuffUntil.get(team);
        return until != null && until.isAfter(Instant.now());
    }

    public double peekSpeedMultiplier(UUID userId, Team team) {
        SpeedBuff buff = speedBuffs.get(userId);
        double individual = buff == null ? 1.0 : buff.multiplier();
        double teamMultiplier = isTeamSpeedBuffActive(team) ? TEAM_POOL_SPEED_MULTIPLIER : 1.0;
        return Math.min(individual, teamMultiplier);
    }

    public void consumeSpeedBuffCharge(UUID userId) {
        speedBuffs.computeIfPresent(userId, (id, buff) -> {
            int remaining = buff.usesRemaining() - 1;
            return remaining > 0 ? new SpeedBuff(buff.multiplier(), remaining) : null;
        });
    }
}
