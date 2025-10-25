package com.youthconnect.edge_functions.exception;

import lombok.Getter;

/**
 * Exception thrown when communication with downstream microservices fails
 *
 * This exception wraps failures when calling other services via Feign clients,
 * providing context about which service failed and what operation was attempted.
 *
 * Use cases:
 * - User Service unreachable during profile fetch
 * - Opportunity Service timeout during application submission
 * - Notification Service failure during alert sending
 * - Mentor Service unavailable during session booking
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@Getter
public class ServiceCommunicationException extends RuntimeException {

    /**
     * Name of the service that failed (e.g., "user-service", "opportunity-service")
     */
    private final String serviceName;

    /**
     * Operation that was being attempted (e.g., "getUserProfile", "createApplication")
     */
    private final String operation;

    /**
     * Whether this operation can be retried
     */
    private final boolean retryable;

    /**
     * HTTP status code from the failed service (if available)
     */
    private final Integer statusCode;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    /**
     * Basic constructor with service name and message
     *
     * @param serviceName Name of the failing service
     * @param message Error description
     */
    public ServiceCommunicationException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
        this.operation = "unknown";
        this.retryable = true; // Default to retryable
        this.statusCode = null;
    }

    /**
     * Constructor with service name, operation, and message
     *
     * @param serviceName Name of the failing service
     * @param operation Operation being attempted
     * @param message Error description
     */
    public ServiceCommunicationException(String serviceName, String operation, String message) {
        super(message);
        this.serviceName = serviceName;
        this.operation = operation;
        this.retryable = true;
        this.statusCode = null;
    }

    /**
     * Constructor with cause (wraps underlying exception)
     *
     * @param serviceName Name of the failing service
     * @param operation Operation being attempted
     * @param message Error description
     * @param cause Underlying exception
     */
    public ServiceCommunicationException(String serviceName, String operation, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.operation = operation;
        this.retryable = determineRetryability(cause);
        this.statusCode = extractStatusCode(cause);
    }

    /**
     * Full constructor with all parameters
     *
     * @param serviceName Name of the failing service
     * @param operation Operation being attempted
     * @param message Error description
     * @param retryable Whether operation can be retried
     * @param statusCode HTTP status code (if applicable)
     */
    public ServiceCommunicationException(
            String serviceName,
            String operation,
            String message,
            boolean retryable,
            Integer statusCode
    ) {
        super(message);
        this.serviceName = serviceName;
        this.operation = operation;
        this.retryable = retryable;
        this.statusCode = statusCode;
    }

    /**
     * Full constructor with cause
     *
     * @param serviceName Name of the failing service
     * @param operation Operation being attempted
     * @param message Error description
     * @param cause Underlying exception
     * @param retryable Whether operation can be retried
     * @param statusCode HTTP status code (if applicable)
     */
    public ServiceCommunicationException(
            String serviceName,
            String operation,
            String message,
            Throwable cause,
            boolean retryable,
            Integer statusCode
    ) {
        super(message, cause);
        this.serviceName = serviceName;
        this.operation = operation;
        this.retryable = retryable;
        this.statusCode = statusCode;
    }

    // ============================================
    // FACTORY METHODS (CONVENIENCE CONSTRUCTORS)
    // ============================================

    /**
     * Create exception for service timeout
     */
    public static ServiceCommunicationException timeout(String serviceName, String operation) {
        return new ServiceCommunicationException(
                serviceName,
                operation,
                String.format("Service %s timed out during operation: %s", serviceName, operation),
                true,
                504
        );
    }

    /**
     * Create exception for service unavailable
     */
    public static ServiceCommunicationException unavailable(String serviceName, String operation) {
        return new ServiceCommunicationException(
                serviceName,
                operation,
                String.format("Service %s is unavailable for operation: %s", serviceName, operation),
                true,
                503
        );
    }

    /**
     * Create exception for bad request to service
     */
    public static ServiceCommunicationException badRequest(String serviceName, String operation, String details) {
        return new ServiceCommunicationException(
                serviceName,
                operation,
                String.format("Invalid request to %s for %s: %s", serviceName, operation, details),
                false, // Don't retry bad requests
                400
        );
    }

    /**
     * Create exception for service returning error
     */
    public static ServiceCommunicationException serviceError(
            String serviceName,
            String operation,
            int statusCode,
            String errorMessage
    ) {
        boolean retryable = statusCode >= 500; // Retry server errors, not client errors

        return new ServiceCommunicationException(
                serviceName,
                operation,
                String.format("Service %s error (%d) during %s: %s",
                        serviceName, statusCode, operation, errorMessage),
                retryable,
                statusCode
        );
    }

    /**
     * Create exception wrapping Feign exception
     */
    public static ServiceCommunicationException fromFeign(
            String serviceName,
            String operation,
            feign.FeignException feignException
    ) {
        int status = feignException.status();
        boolean retryable = status >= 500 || status == 429; // Retry server errors and rate limits

        return new ServiceCommunicationException(
                serviceName,
                operation,
                String.format("Feign call to %s failed: %s", serviceName, feignException.getMessage()),
                feignException,
                retryable,
                status
        );
    }

    // ============================================
    // UTILITY METHODS
    // ============================================

    /**
     * Determine if exception should allow retry based on cause
     */
    private static boolean determineRetryability(Throwable cause) {
        if (cause == null) return true;

        // Network errors are retryable
        if (cause instanceof java.net.SocketTimeoutException ||
                cause instanceof java.net.ConnectException ||
                cause instanceof java.io.IOException) {
            return true;
        }

        // Feign exceptions depend on status code
        if (cause instanceof feign.FeignException) {
            int status = ((feign.FeignException) cause).status();
            return status >= 500 || status == 429; // Server errors and rate limits
        }

        return true; // Default to retryable
    }

    /**
     * Extract HTTP status code from exception cause
     */
    private static Integer extractStatusCode(Throwable cause) {
        if (cause instanceof feign.FeignException) {
            return ((feign.FeignException) cause).status();
        }
        return null;
    }

    /**
     * Get user-friendly error message
     */
    public String getUserFriendlyMessage() {
        if (statusCode != null && statusCode >= 400 && statusCode < 500) {
            return "Invalid request. Please check your input and try again.";
        } else if (statusCode != null && statusCode >= 500) {
            return String.format("The %s service is experiencing issues. Please try again later.", serviceName);
        } else {
            return String.format("Unable to connect to %s. Please check your connection and try again.", serviceName);
        }
    }

    /**
     * Check if this is a client error (4xx)
     */
    public boolean isClientError() {
        return statusCode != null && statusCode >= 400 && statusCode < 500;
    }

    /**
     * Check if this is a server error (5xx)
     */
    public boolean isServerError() {
        return statusCode != null && statusCode >= 500;
    }

    /**
     * Check if this is a timeout
     */
    public boolean isTimeout() {
        return statusCode != null && (statusCode == 504 || statusCode == 408);
    }

    @Override
    public String toString() {
        return String.format("ServiceCommunicationException{serviceName='%s', operation='%s', retryable=%s, statusCode=%d, message='%s'}",
                serviceName, operation, retryable, statusCode, getMessage());
    }
}