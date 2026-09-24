package com.opt.backend.common.exception;

public class SpeedBuffAlreadyActiveException extends RuntimeException {

    public SpeedBuffAlreadyActiveException() {
        super("Un effet Cafe/Rafale est deja actif, attends qu'il se termine avant d'en racheter un");
    }
}
