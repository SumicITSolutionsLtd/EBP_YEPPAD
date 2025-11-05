package com.youthconnect.job_services.dto.response;

import com.youthconnect.job_services.enums.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Job Response DTO - Updated with UUID
 *
 * Used for job listings (summary view).
 * Contains essential information without full details.
 * All identifiers updated to UUID following platform guidelines.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
@Builder
public class JobResponse {

    /**
     * Job identifier (UUID)
     */
    private UUID jobId;

    private String jobTitle;
    private String companyName;

    // Job Classification
    private JobType jobType;
    private WorkMode workMode;
    private String location;

    // Category (UUID)
    private UUID categoryId;
    private String categoryName;

    // Salary (if shown)
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private SalaryPeriod salaryPeriod;
    private Boolean showSalary;

    // Status
    private JobStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;

    // Engagement Metrics
    private Integer applicationCount;
    private Integer viewCount;

    // Flags
    private Boolean isFeatured;
    private Boolean isUrgent;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    /**
     * Check if job has expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if job is accepting applications
     */
    public boolean isAcceptingApplications() {
        return JobStatus.PUBLISHED.equals(status) && !isExpired();
    }

    /**
     * Get formatted salary string
     */
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