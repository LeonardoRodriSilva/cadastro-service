package com.exception;

public class DatabaseConfigException extends RuntimeException {
    public DatabaseConfigException(String message) {
        super(message);
    }

    public DatabaseConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
