package com.youthconnect.opportunity_service.exception;

/**
 * Exception thrown when a user attempts an unauthorized action
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}