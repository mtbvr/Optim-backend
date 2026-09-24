package com.opt.backend.common.exception;

public class SkinNotPurchasableException extends RuntimeException {

    public SkinNotPurchasableException() {
        super("Ce skin ne s'achete pas, il se deverrouille via un succes");
    }
}
