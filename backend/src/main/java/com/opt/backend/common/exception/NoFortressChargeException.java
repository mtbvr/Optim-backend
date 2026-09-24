package com.opt.backend.common.exception;

public class NoFortressChargeException extends RuntimeException {

    public NoFortressChargeException() {
        super("Aucune charge de forteresse disponible, achete la dans la boutique de perks");
    }
}
