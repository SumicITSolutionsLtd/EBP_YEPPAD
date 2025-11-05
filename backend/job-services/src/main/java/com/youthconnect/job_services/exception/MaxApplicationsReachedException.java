package com.youthconnect.job_services.exception;

import java.util.UUID;

/**
 * MaxApplicationsReachedException - FIXED with UUID Support
 *
 * Thrown when a job posting has reached its maximum allowed number of applications.
 * HTTP Status: 400 BAD REQUEST
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
public class MaxApplicationsReachedException extends RuntimeException {

    /**
     * Constructor with custom message
     */
    public MaxApplicationsReachedException(String message) {
        super(message);
    }

    /**
     * Constructor with UUID job ID and max applications count
     */
    public MaxApplicationsReachedException(UUID jobId, int maxApplications) {
        super(String.format(
                "Job %s has reached its maximum applications limit of %d. " +
                        "No more applications can be submitted at this time.",
                jobId,
                maxApplications
        ));
    }

    /**
     * Constructor with UUID and job details
     */
    public MaxApplicationsReachedException(
            UUID jobId,
            String jobTitle,
            int currentApplications,
            int maxApplications) {
        super(String.format(
                "Job '%s' (ID: %s) has reached maximum capacity with %d/%d applications. " +
                        "Please check back later or explore other opportunities.",
                jobTitle,
                jobId,
                currentApplications,
                maxApplications
        ));
    }
}