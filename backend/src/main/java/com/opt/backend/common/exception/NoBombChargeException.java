package com.opt.backend.common.exception;

public class NoBombChargeException extends RuntimeException {

    public NoBombChargeException() {
        super("Aucune charge de bombe disponible, achete la dans la boutique de perks");
    }
}
