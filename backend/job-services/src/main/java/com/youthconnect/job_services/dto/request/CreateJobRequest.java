package com.youthconnect.job_services.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import com.youthconnect.job_services.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Create Job Request DTO - FIXED with UUID
 *
 * ✅ CHANGED: categoryId from Long to UUID
 *
 * Request body for creating a new job posting.
 * Uses UUID for category reference following platform guidelines.
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (UUID Migration - FIXED)
 */
@Data
public class CreateJobRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 255, message = "Job title must not exceed 255 characters")
    private String jobTitle;

    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;

    @NotBlank(message = "Job description is required")
    @Size(min = 100, message = "Job description must be at least 100 characters")
    private String jobDescription;

    private String responsibilities;
    private String requirements;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotNull(message = "Work mode is required (Remote, Onsite, Hybrid)")
    private WorkMode workMode;

    private String location;  // Required if workMode is ONSITE or HYBRID

    /**
     * Category ID using UUID
     * ✅ FIXED: Changed from Long to UUID
     */
    @NotNull(message = "Category is required")
    private UUID categoryId;  // ✅ FIXED: Was Long, now UUID

    // Salary Information (optional)
    @DecimalMin(value = "0.0", message = "Minimum salary must be non-negative")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.0", message = "Maximum salary must be non-negative")
    private BigDecimal salaryMax;

    private String salaryCurrency = "UGX";
    private SalaryPeriod salaryPeriod;
    private Boolean showSalary = false;

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
    @NotNull(message = "Expiration date is required")
    private LocalDateTime expiresAt;

    @Min(value = 0, message = "Max applications must be non-negative (0 = unlimited)")
    private Integer maxApplications = 0;

    private Boolean isFeatured = false;
    private Boolean isUrgent = false;

    /**
     * Custom validation: Location is required for ONSITE and HYBRID work modes
     */
    @AssertTrue(message = "Location is required for Onsite and Hybrid jobs")
    private boolean isLocationValid() {
        if (workMode == WorkMode.ONSITE || workMode == WorkMode.HYBRID) {
            return location != null && !location.trim().isEmpty();
        }
        return true;
    }

    /**
     * Custom validation: At least one application method must be provided
     */
    @AssertTrue(message = "At least one application method (email, phone, or URL) is required")
    private boolean isApplicationMethodProvided() {
        return (applicationEmail != null && !applicationEmail.trim().isEmpty()) ||
                (applicationPhone != null && !applicationPhone.trim().isEmpty()) ||
                (applicationUrl != null && !applicationUrl.trim().isEmpty());
    }

    /**
     * Custom validation: Salary max should be greater than or equal to salary min
     */
    @AssertTrue(message = "Maximum salary must be greater than or equal to minimum salary")
    private boolean isSalaryRangeValid() {
        if (salaryMin != null && salaryMax != null) {
            return salaryMax.compareTo(salaryMin) >= 0;
        }
        return true;
    }
}