package com.youthconnect.job_services.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

/**
 * Create Application Request DTO - Updated with UUID
 *
 * Request body for submitting a job application.
 * Uses UUID for job and file references.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
public class CreateApplicationRequest {

    /**
     * Job ID using UUID
     */
    @NotNull(message = "Job ID is required")
    private UUID jobId;

    @NotBlank(message = "Cover letter is required")
    @Size(min = 50, max = 5000, message = "Cover letter must be between 50-5000 characters")
    private String coverLetter;

    /**
     * Reference to resume file stored in file-management-service (UUID)
     */
    private UUID resumeFileId;

    // Optional: Additional application details
    @Size(max = 2000, message = "Additional information cannot exceed 2000 characters")
    private String additionalInfo;

    // Optional: Expected salary (for jobs with salary negotiation)
    private java.math.BigDecimal expectedSalary;

    // Optional: Availability start date
    private java.time.LocalDate availableFrom;
}