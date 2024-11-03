package com.emamagic.exception;

public class InstanceOfUtilityClassException extends RuntimeException {
    public InstanceOfUtilityClassException(String className) {
        super(String.format("can not create a instance from a utility class: %s", className));
    }
}
