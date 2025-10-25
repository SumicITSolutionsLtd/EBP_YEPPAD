package com.youthconnect.opportunity_service.exception;

/**
 * Exception thrown when an opportunity is not found
 */
public class OpportunityNotFoundException extends RuntimeException {
    public OpportunityNotFoundException(String message) {
        super(message);
    }

    public OpportunityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}