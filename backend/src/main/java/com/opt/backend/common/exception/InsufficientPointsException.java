package com.opt.backend.common.exception;

public class InsufficientPointsException extends RuntimeException {

    private final int cost;
    private final int available;

    public InsufficientPointsException(int cost, int available) {
        super("Points insuffisants : " + cost + " requis, " + available + " disponibles");
        this.cost = cost;
        this.available = available;
    }

    public int getCost() {
        return cost;
    }

    public int getAvailable() {
        return available;
    }
}
