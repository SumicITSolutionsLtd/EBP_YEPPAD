package com.youthconnect.job_services.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * JobExpiredException - FIXED with UUID Support
 *
 * Thrown when attempting to apply to or interact with an expired job posting.
 * HTTP Status: 400 BAD REQUEST
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
public class JobExpiredException extends RuntimeException {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Constructor with custom message
     */
    public JobExpiredException(String message) {
        super(message);
    }

    /**
     * Constructor with UUID job ID
     */
    public JobExpiredException(UUID jobId) {
        super(String.format(
                "Job %s has expired and is no longer accepting applications",
                jobId
        ));
    }

    /**
     * Constructor with UUID and job details
     */
    public JobExpiredException(UUID jobId, String jobTitle, LocalDateTime expiresAt) {
        super(String.format(
                "Job '%s' (ID: %s) expired on %s. No new applications can be submitted.",
                jobTitle,
                jobId,
                expiresAt.format(FORMATTER)
        ));
    }
}