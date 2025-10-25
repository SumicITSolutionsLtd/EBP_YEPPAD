package com.youthconnect.edge_functions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Opportunity Data Transfer Object
 *
 * FIXED ISSUES:
 * - Added missing 'opportunityType' field (was incorrectly named 'type')
 * - Added 'employerId' field for job opportunities
 * - Proper field naming conventions aligned with database schema
 *
 * Matches the opportunity-service structure exactly
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityDTO {

    // ============================================
    // CORE IDENTIFICATION
    // ============================================

    /**
     * Unique opportunity identifier
     */
    private Long opportunityId;

    /**
     * ID of the user/organization that posted the opportunity
     * Can be NGO, Funder, or Service Provider
     */
    private Long postedById;

    // ============================================
    // OPPORTUNITY DETAILS
    // ============================================

    /**
     * Opportunity title (10-255 characters)
     */
    private String title;

    /**
     * Detailed description (max 5000 characters)
     */
    private String description;

    /**
     * Requirements for applicants
     */
    private String requirements;

    /**
     * Physical location or address
     */
    private String location;

    /**
     * Ugandan district where opportunity is available
     */
    private String district;

    // ============================================
    // CLASSIFICATION (FIXED)
    // ============================================

    /**
     * Type of opportunity
     * FIXED: Changed from 'type' to 'opportunityType' to match backend
     *
     * Values: GRANT, LOAN, JOB, TRAINING, SKILL_MARKET
     */
    private String opportunityType;

    // ============================================
    // FINANCIAL DETAILS
    // ============================================

    /**
     * Minimum salary for jobs or funding amount for grants/loans
     */
    private BigDecimal salaryMin;

    /**
     * Maximum salary for jobs
     */
    private BigDecimal salaryMax;

    /**
     * Total funding amount available
     */
    private BigDecimal fundingAmount;

    /**
     * Currency code (default: UGX)
     */
    private String currency;

    // ============================================
    // STATUS AND DEADLINES
    // ============================================

    /**
     * Current status of opportunity
     * Values: DRAFT, OPEN, CLOSED, IN_REVIEW, COMPLETED
     */
    private String status;

    /**
     * Deadline for applications
     */
    private LocalDateTime applicationDeadline;

    /**
     * Maximum number of applicants (null = unlimited)
     */
    private Integer maxApplicants;

    /**
     * Current number of applications received
     */
    private Integer applicationCount;

    // ============================================
    // CONTACT INFORMATION
    // ============================================

    /**
     * Contact information for inquiries
     */
    private String contactInfo;

    /**
     * Preferred contact method (email, phone, sms)
     */
    private String contactMethod;

    // ============================================
    // EMPLOYER-SPECIFIC (FIXED - ADDED)
    // ============================================

    /**
     * Employer ID for job opportunities
     * FIXED: Added this field for job postings
     */
    private Long employerId;

    // ============================================
    // TIMESTAMPS
    // ============================================

    /**
     * When opportunity was created
     */
    private LocalDateTime createdAt;

    /**
     * When opportunity was last updated
     */
    private LocalDateTime updatedAt;

    // ============================================
    // COMPUTED FIELDS (for UI)
    // ============================================

    /**
     * Days remaining until deadline
     */
    public Integer getDaysUntilDeadline() {
        if (applicationDeadline == null) return null;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now(),
                applicationDeadline
        );
    }

    /**
     * Check if opportunity is currently accepting applications
     */
    public boolean isAcceptingApplications() {
        if (!"OPEN".equals(status)) return false;
        if (applicationDeadline != null && applicationDeadline.isBefore(LocalDateTime.now())) {
            return false;
        }
        if (maxApplicants != null && applicationCount != null) {
            return applicationCount < maxApplicants;
        }
        return true;
    }

    /**
     * Get display name for opportunity type
     */
    public String getOpportunityTypeDisplay() {
        if (opportunityType == null) return "Unknown";

        switch (opportunityType.toUpperCase()) {
            case "GRANT": return "Grant/Funding";
            case "LOAN": return "Loan/Credit";
            case "JOB": return "Employment Opportunity";
            case "TRAINING": return "Training Program";
            case "SKILL_MARKET": return "Skills Marketplace";
            default: return opportunityType;
        }
    }
}