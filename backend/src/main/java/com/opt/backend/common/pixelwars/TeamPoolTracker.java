package com.opt.backend.common.pixelwars;

import com.opt.backend.common.config.PixelWarsProperties;
import com.opt.backend.common.websocket.PixelWebSocketHandler;
import com.opt.backend.dto.TeamPoolTriggeredEvent;
import com.opt.backend.dto.TeamPoolUpdateEvent;
import com.opt.backend.dto.TeamPoolsDto;
import com.opt.backend.entity.Team;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TeamPoolTracker {

    private final PixelWarsProperties properties;
    private final PerkEffectsTracker perkEffectsTracker;
    private final PixelWebSocketHandler webSocketHandler;
    private final Map<Team, AtomicInteger> pool = new ConcurrentHashMap<>();

    public TeamPoolTracker(PixelWarsProperties properties, PerkEffectsTracker perkEffectsTracker, PixelWebSocketHandler webSocketHandler) {
        this.properties = properties;
        this.perkEffectsTracker = perkEffectsTracker;
        this.webSocketHandler = webSocketHandler;
        for (Team team : Team.values()) {
            pool.put(team, new AtomicInteger());
        }
    }

    public TeamPoolsDto snapshot() {
        Map<Team, Integer> result = new EnumMap<>(Team.class);
        pool.forEach((team, amount) -> result.put(team, amount.get()));
        return new TeamPoolsDto(result, properties.getTeamPoolThreshold());
    }

    public void contribute(Team team, int amount) {
        int threshold = properties.getTeamPoolThreshold();
        int newValue = pool.get(team).addAndGet(amount);

        if (newValue >= threshold) {
            pool.get(team).set(0);
            webSocketHandler.broadcast(TeamPoolUpdateEvent.of(team, 0, threshold));
            Duration duration = Duration.ofSeconds(properties.getTeamPoolBuffDurationSeconds());
            perkEffectsTracker.activateTeamSpeedBuff(team, duration);
            webSocketHandler.broadcast(TeamPoolTriggeredEvent.of(team, System.currentTimeMillis() + duration.toMillis()));
        } else {
            webSocketHandler.broadcast(TeamPoolUpdateEvent.of(team, newValue, threshold));
        }
    }
}
