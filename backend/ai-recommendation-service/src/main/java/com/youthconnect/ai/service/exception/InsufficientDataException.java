package com.youthconnect.ai.service.exception;

/**
 * Exception thrown when insufficient data available for recommendations
 *
 * @author Douglas Kings Kato
 */
public class InsufficientDataException extends AIRecommendationException {

    public InsufficientDataException(String message) {
        super("INSUFFICIENT_DATA", message);
    }

    public InsufficientDataException(Long userId, String dataType) {
        super("INSUFFICIENT_DATA",
                String.format("Insufficient %s data for user: %d", dataType, userId));
    }
}