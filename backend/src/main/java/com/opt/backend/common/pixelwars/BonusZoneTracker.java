package com.opt.backend.common.pixelwars;

import com.opt.backend.common.config.PixelWarsProperties;
import com.opt.backend.common.websocket.PixelWebSocketHandler;
import com.opt.backend.dto.BonusZoneActiveEvent;
import com.opt.backend.dto.BonusZoneClearedEvent;
import com.opt.backend.dto.BonusZoneDto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
public class BonusZoneTracker {

    private final PixelWarsProperties properties;
    private final BoardGrid boardGrid;
    private final PixelWebSocketHandler webSocketHandler;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private volatile BonusZoneDto activeZone;
    private volatile ScheduledFuture<?> pendingClear;

    public BonusZoneTracker(PixelWarsProperties properties, BoardGrid boardGrid, PixelWebSocketHandler webSocketHandler) {
        this.properties = properties;
        this.boardGrid = boardGrid;
        this.webSocketHandler = webSocketHandler;
    }

    public BonusZoneDto getActiveZone() {
        return activeZone;
    }

    public boolean isActiveAt(int x, int y) {
        BonusZoneDto zone = activeZone;
        return zone != null && zone.x() == x && zone.y() == y;
    }

    @Scheduled(fixedDelayString = "${app.pixel-wars.bonus-zone-interval-seconds:60}000")
    void spawnZone() {
        if (pendingClear != null) {
            pendingClear.cancel(false);
        }
        int x = ThreadLocalRandom.current().nextInt(boardGrid.getWidth());
        int y = ThreadLocalRandom.current().nextInt(boardGrid.getHeight());
        int durationSeconds = properties.getBonusZoneDurationSeconds();

        activeZone = new BonusZoneDto(x, y, System.currentTimeMillis() + durationSeconds * 1000L);
        webSocketHandler.broadcast(BonusZoneActiveEvent.of(activeZone));
        pendingClear = scheduler.schedule(this::clearZone, durationSeconds, TimeUnit.SECONDS);
    }

    private void clearZone() {
        activeZone = null;
        webSocketHandler.broadcast(BonusZoneClearedEvent.of());
    }
}
