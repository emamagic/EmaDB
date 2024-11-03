package com.emamagic.exception;

public class ConfigNotFoundException extends RuntimeException {
    public ConfigNotFoundException(String message) {
        super(message);
    }
}
