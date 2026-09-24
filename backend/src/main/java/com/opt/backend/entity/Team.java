package com.opt.backend.entity;

public enum Team {
    RED,
    BLUE;

    public Team opposite() {
        return this == RED ? BLUE : RED;
    }
}
