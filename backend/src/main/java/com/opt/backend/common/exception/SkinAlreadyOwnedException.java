package com.opt.backend.common.exception;

public class SkinAlreadyOwnedException extends RuntimeException {

    public SkinAlreadyOwnedException() {
        super("Tu possedes deja ce skin");
    }
}
