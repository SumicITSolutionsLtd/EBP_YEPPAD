package com.youthconnect.job_services.exception;

import java.util.UUID;

/**
 * DuplicateApplicationException
 *
 * Thrown when a user tries to apply to a job they've already applied to.
 * HTTP Status: 409 CONFLICT
 */
public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(String message) {
        super(message);
    }

    public DuplicateApplicationException(UUID userId, UUID jobId) {
        super(String.format("User %d has already applied to job %d", userId, jobId));
    }
}