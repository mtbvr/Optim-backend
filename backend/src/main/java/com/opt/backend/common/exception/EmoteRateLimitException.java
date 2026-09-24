package com.opt.backend.common.exception;

public class EmoteRateLimitException extends RuntimeException {

    public EmoteRateLimitException() {
        super("Attends un instant avant d'envoyer une nouvelle reaction");
    }
}
