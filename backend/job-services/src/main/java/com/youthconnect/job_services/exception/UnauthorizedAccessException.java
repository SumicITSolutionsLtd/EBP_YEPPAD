package com.youthconnect.job_services.exception;

/**
 * UnauthorizedAccessException
 *
 * Thrown when a user attempts an action they don't have permission for.
 * HTTP Status: 403 FORBIDDEN
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String action, String resource) {
        super(String.format("Unauthorized to perform '%s' on %s", action, resource));
    }
}