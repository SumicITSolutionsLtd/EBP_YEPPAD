package com.youthconnect.ai.service.exception;

/**
 * Base Exception for AI Recommendation Service
 *
 * All custom exceptions in this service should extend this class
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
public class AIRecommendationException extends RuntimeException {

    private String errorCode;

    public AIRecommendationException(String message) {
        super(message);
    }

    public AIRecommendationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AIRecommendationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AIRecommendationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}