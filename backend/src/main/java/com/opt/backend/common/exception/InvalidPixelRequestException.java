package com.opt.backend.common.exception;

public class InvalidPixelRequestException extends RuntimeException {

    private final String code;

    public InvalidPixelRequestException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
