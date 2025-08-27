package com.exception;

public class RedisConfigException extends RuntimeException {

    public RedisConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}