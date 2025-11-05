package com.youthconnect.ai.service.exception;

/**
 * Exception thrown when recommendation generation fails
 *
 * @author Douglas Kings Kato
 */
public class RecommendationGenerationException extends AIRecommendationException {

    public RecommendationGenerationException(String message) {
        super("RECOMMENDATION_FAILED", message);
    }

    public RecommendationGenerationException(String message, Throwable cause) {
        super("RECOMMENDATION_FAILED", message, cause);
    }
}