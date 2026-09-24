package com.opt.backend.common.exception;

public class InvalidEmoteException extends RuntimeException {

    public InvalidEmoteException() {
        super("Reaction non autorisee");
    }
}
