package com.opt.backend.common.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    private final String email;

    public EmailAlreadyUsedException(String email) {
        super("Un compte existe deja avec l'email " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
