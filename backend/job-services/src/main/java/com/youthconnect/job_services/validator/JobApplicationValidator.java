package com.youthconnect.job_services.validator;

import com.youthconnect.job_services.entity.Job;
import com.youthconnect.job_services.exception.InvalidJobStatusException;
import com.youthconnect.job_services.exception.JobExpiredException;
import com.youthconnect.job_services.exception.MaxApplicationsReachedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Job Application Validator - FIXED with UUID Support
 *
 * Validates whether a job is eligible to receive applications.
 * All UUID-based validations.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Component
public class JobApplicationValidator {

    /**
     * Validates if a user can apply to a given job
     * FIXED: Uses UUID for userId
     */
    public void validateApplicationEligibility(Job job, UUID userId) {
        if (!job.isAcceptingApplications()) {

            if (job.isExpired()) {
                throw new JobExpiredException(
                        job.getJobId(),  // UUID
                        job.getJobTitle(),
                        job.getExpiresAt()
                );
            }

            if (job.hasReachedMaxApplications()) {
                throw new MaxApplicationsReachedException(
                        job.getJobId(),  // UUID
                        job.getJobTitle(),
                        job.getApplicationCount(),
                        job.getMaxApplications()
                );
            }

            throw new InvalidJobStatusException(
                    job.getJobId(),  // UUID
                    "apply",
                    job.getStatus()
            );
        }
    }

    /**
     * Validates if a job can be published
     */
    public void validateJobPublish(Job job) {
        if (job.getStatus() != com.youthconnect.job_services.enums.JobStatus.DRAFT) {
            throw new InvalidJobStatusException(
                    "publish",
                    job.getStatus().name(),
                    "DRAFT"
            );
        }

        if (job.getExpiresAt() != null && job.isExpired()) {
            throw new IllegalStateException("Cannot publish job with past expiry date");
        }
    }

    /**
     * Validates if a job can be closed
     */
    public void validateJobClose(Job job) {
        if (job.getStatus() != com.youthconnect.job_services.enums.JobStatus.PUBLISHED) {
            throw new InvalidJobStatusException(
                    "close",
                    job.getStatus().name(),
                    "PUBLISHED"
            );
        }
    }
}