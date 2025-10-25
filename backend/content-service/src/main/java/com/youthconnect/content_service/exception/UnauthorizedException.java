package com.youthconnect.content_service.exception;

/**
 * Exception thrown when user is not authorized for an action
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}