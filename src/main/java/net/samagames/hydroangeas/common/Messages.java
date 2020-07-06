package net.samagames.hydroangeas.common;

public enum Messages {
    HUB("hub");

    private final String message;

    Messages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
