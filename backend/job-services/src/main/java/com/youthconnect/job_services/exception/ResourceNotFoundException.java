package com.youthconnect.job_services.exception;

/**
 * ResourceNotFoundException
 *
 * Thrown when a requested resource (job, application, category) is not found.
 * HTTP Status: 404 NOT FOUND
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}

