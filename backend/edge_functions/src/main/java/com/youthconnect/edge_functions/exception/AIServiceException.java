package com.youthconnect.edge_functions.exception;

/**
 * Exception thrown when AI service operations fail
 *
 * Covers scenarios such as:
 * - OpenAI API unavailability
 * - Rate limit exceeded
 * - Invalid API key
 * - Model errors
 * - Timeout issues
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
public class AIServiceException extends RuntimeException {

    private final String errorCode;
    private final Integer httpStatus;

    /**
     * Constructor with message only
     */
    public AIServiceException(String message) {
        super(message);
        this.errorCode = "AI_SERVICE_ERROR";
        this.httpStatus = 503;
    }

    /**
     * Constructor with message and cause
     */
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AI_SERVICE_ERROR";
        this.httpStatus = 503;
    }

    /**
     * Constructor with full error details
     */
    public AIServiceException(String message, String errorCode, Integer httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * Constructor with full error details and cause
     */
    public AIServiceException(String message, Throwable cause, String errorCode, Integer httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    /**
     * Factory method for API key errors
     */
    public static AIServiceException apiKeyNotConfigured() {
        return new AIServiceException(
                "OpenAI API key not configured. Please set OPENAI_API_KEY environment variable.",
                "AI_API_KEY_MISSING",
                500
        );
    }

    /**
     * Factory method for rate limit errors
     */
    public static AIServiceException rateLimitExceeded() {
        return new AIServiceException(
                "OpenAI API rate limit exceeded. Please try again later.",
                "AI_RATE_LIMIT_EXCEEDED",
                429
        );
    }

    /**
     * Factory method for timeout errors
     */
    public static AIServiceException timeout() {
        return new AIServiceException(
                "OpenAI API request timed out. Please try again.",
                "AI_TIMEOUT",
                504
        );
    }

    /**
     * Factory method for invalid request errors
     */
    public static AIServiceException invalidRequest(String details) {
        return new AIServiceException(
                "Invalid AI service request: " + details,
                "AI_INVALID_REQUEST",
                400
        );
    }
}