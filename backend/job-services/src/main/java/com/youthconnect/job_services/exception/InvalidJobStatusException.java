package com.youthconnect.job_services.exception;

import java.util.UUID;

/**
 * InvalidJobStatusException - FIXED with UUID Support
 *
 * Thrown when attempting an operation on a job with an invalid status.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
public class InvalidJobStatusException extends RuntimeException {

    /**
     * Constructor with custom error message
     */
    public InvalidJobStatusException(String message) {
        super(message);
    }

    /**
     * Constructor with UUID job ID and operation context
     */
    public InvalidJobStatusException(UUID jobId, String operation, Enum<?> jobStatus) {
        super(String.format(
                "Cannot perform '%s' on job %s with status '%s'",
                operation,
                jobId,
                jobStatus.name()
        ));
    }

    /**
     * Constructor with operation and status requirements
     */
    public InvalidJobStatusException(String operation, String currentStatus, String requiredStatus) {
        super(String.format(
                "Cannot perform '%s' on job with status '%s'. Required status: '%s'",
                operation,
                currentStatus,
                requiredStatus
        ));
    }
}