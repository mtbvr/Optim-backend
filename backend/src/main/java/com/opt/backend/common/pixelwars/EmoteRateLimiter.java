package com.opt.backend.common.pixelwars;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmoteRateLimiter {

    private static final long MIN_INTERVAL_SECONDS = 2;

    private final Map<UUID, Instant> lastEmoteByUser = new ConcurrentHashMap<>();

    public boolean tryAcquire(UUID userId) {
        Instant now = Instant.now();
        Instant last = lastEmoteByUser.get(userId);
        if (last != null && now.getEpochSecond() - last.getEpochSecond() < MIN_INTERVAL_SECONDS) {
            return false;
        }
        lastEmoteByUser.put(userId, now);
        return true;
    }
}
