package com.youthconnect.opportunity_service.exception;

/**
 * Exception thrown when an application is invalid or violates business rules
 */
public class InvalidApplicationException extends RuntimeException {
    public InvalidApplicationException(String message) {
        super(message);
    }

    public InvalidApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}