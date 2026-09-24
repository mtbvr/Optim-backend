package com.opt.backend.common.exception;

public class SkinNotOwnedException extends RuntimeException {

    public SkinNotOwnedException() {
        super("Tu ne possedes pas encore ce skin, achete le d'abord");
    }
}
