package com.opt.backend.common.exception;

public class CooldownActiveException extends RuntimeException {

    private final long remainingSeconds;

    public CooldownActiveException(long remainingSeconds) {
        super("Cooldown actif, reessayez dans " + remainingSeconds + " secondes");
        this.remainingSeconds = remainingSeconds;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }
}
