package com.opt.backend.common.pixelwars;

import org.springframework.stereotype.Component;

@Component
public class GlobalPlacementCounter {

    private long total = 0;

    public synchronized long incrementAndGet() {
        total++;
        return total;
    }
}
