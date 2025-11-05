package com.youthconnect.job_services.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import com.youthconnect.job_services.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Update Job Request DTO - FIXED with UUID
 *
 * ✅ CHANGED: categoryId from Long to UUID
 *
 * Request body for updating an existing job posting.
 * All fields are optional - only provided fields will be updated.
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (UUID Migration - FIXED)
 */
@Data
public class UpdateJobRequest {

    @Size(max = 255, message = "Job title must not exceed 255 characters")
    private String jobTitle;

    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;

    @Size(min = 100, message = "Job description must be at least 100 characters")
    private String jobDescription;

    private String responsibilities;
    private String requirements;

    private JobType jobType;
    private WorkMode workMode;
    private String location;

    /**
     * Category ID using UUID
     * ✅ FIXED: Changed from @Positive Long to UUID
     */
    private UUID categoryId;  // ✅ FIXED: Was Long, now UUID

    // Salary Information
    @DecimalMin(value = "0.0", message = "Minimum salary must be non-negative")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.0", message = "Maximum salary must be non-negative")
    private BigDecimal salaryMax;

    private String salaryCurrency;
    private SalaryPeriod salaryPeriod;
    private Boolean showSalary;

    // Requirements
    private String experienceRequired;
    private EducationLevel educationLevel;

    // Application Details
    @Email(message = "Invalid email format")
    private String applicationEmail;

    @Pattern(regexp = "^\\+?256[0-9]{9}$|^0[0-9]{9}$",
            message = "Invalid Uganda phone number")
    private String applicationPhone;

    @Pattern(regexp = "^https?://.*", message = "Invalid URL format")
    private String applicationUrl;

    private String howToApply;

    // Job Posting Settings
    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiresAt;

    @Min(value = 0, message = "Max applications must be non-negative (0 = unlimited)")
    private Integer maxApplications;

    private Boolean isFeatured;
    private Boolean isUrgent;
    private JobStatus status;
}