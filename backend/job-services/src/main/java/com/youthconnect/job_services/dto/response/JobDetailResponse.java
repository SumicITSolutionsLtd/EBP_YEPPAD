package com.youthconnect.job_services.dto.response;

import com.youthconnect.job_services.enums.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Job Detail Response DTO - Updated with UUID
 *
 * Complete job information with all details.
 * Used for single job view with full information.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
@Builder
public class JobDetailResponse {

    /**
     * Job identifier (UUID)
     */
    private UUID jobId;
    private String jobTitle;
    private String companyName;

    // Poster Information (UUID)
    private UUID postedByUserId;
    private UserRole postedByRole;

    // Job Content
    private String jobDescription;
    private String responsibilities;
    private String requirements;

    // Job Classification
    private JobType jobType;
    private WorkMode workMode;
    private String location;

    // Category (UUID)
    private UUID categoryId;
    private String categoryName;
    private String categoryIconUrl;

    // Salary
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private SalaryPeriod salaryPeriod;
    private Boolean showSalary;

    // Requirements
    private String experienceRequired;
    private EducationLevel educationLevel;

    // Application Details
    private String applicationEmail;
    private String applicationPhone;
    private String applicationUrl;
    private String howToApply;

    // Status
    private JobStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime closedAt;

    // Application Management
    private Integer maxApplications;
    private Integer applicationCount;
    private Integer viewCount;

    // User-specific data (if authenticated) - UUID
    private Boolean hasApplied;
    private UUID userApplicationId;
    private ApplicationStatus userApplicationStatus;

    // Flags
    private Boolean isFeatured;
    private Boolean isUrgent;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isAcceptingApplications() {
        return JobStatus.PUBLISHED.equals(status) && !isExpired();
    }

    public String getFormattedSalary() {
        if (!Boolean.TRUE.equals(showSalary) || salaryMin == null) {
            return "Not disclosed";
        }

        String formatted = salaryCurrency + " " + salaryMin;
        if (salaryMax != null && !salaryMax.equals(salaryMin)) {
            formatted += " - " + salaryMax;
        }
        if (salaryPeriod != null) {
            formatted += " " + salaryPeriod.name().toLowerCase();
        }
        return formatted;
    }
}