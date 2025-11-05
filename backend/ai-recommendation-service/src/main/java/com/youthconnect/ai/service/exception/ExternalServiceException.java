package com.youthconnect.ai.service.exception;

/**
 * Exception thrown when external service call fails
 *
 * @author Douglas Kings Kato
 */
public class ExternalServiceException extends AIRecommendationException {

    private String serviceName;

    public ExternalServiceException(String serviceName, String message) {
        super("EXTERNAL_SERVICE_ERROR",
                String.format("External service '%s' error: %s", serviceName, message));
        this.serviceName = serviceName;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR",
                String.format("External service '%s' error: %s", serviceName, message),
                cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}