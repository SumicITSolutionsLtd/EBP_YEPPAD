package com.youthconnect.user_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * USER PROFILE RESPONSE DTO - ENHANCED WITH JOB STATISTICS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Comprehensive user profile response including job application statistics.
 * This DTO is returned by user profile endpoints and includes data from
 * both user-service and job-service.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Migration)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    // =========================================================================
    // BASIC USER INFORMATION
    // =========================================================================

    /**
     * User's unique identifier - FIXED: UUID type
     */
    private UUID userId;

    /**
     * User's email address
     */
    private String email;

    /**
     * User's phone number (Uganda format)
     */
    private String phoneNumber;

    /**
     * User role (YOUTH, NGO, MENTOR, etc.)
     */
    private String role;

    /**
     * Account active status
     */
    private Boolean isActive;

    /**
     * Email verification status
     */
    private Boolean emailVerified;

    /**
     * Phone verification status
     */
    private Boolean phoneVerified;

    // =========================================================================
    // PROFILE INFORMATION
    // =========================================================================

    private String firstName;
    private String lastName;
    private String gender;
    private String district;
    private String profession;
    private String businessStage;
    private String description;
    private String profilePictureUrl;
    private Integer profileCompleteness;

    // =========================================================================
    // JOB APPLICATION STATISTICS
    // =========================================================================

    private Integer totalJobApplications;
    private Integer pendingJobApplications;
    private Integer approvedJobApplications;
    private Integer rejectedJobApplications;
    private Double jobApplicationSuccessRate;
    private String currentEmploymentStatus;
    private CurrentJobDetails currentJob;

    // =========================================================================
    // TIMESTAMPS
    // =========================================================================

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

    // =========================================================================
    // NESTED DTOs
    // =========================================================================

    /**
     * Current Job Details DTO - FIXED: UUID for jobId
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CurrentJobDetails {
        private UUID jobId;  // FIXED: Changed from Long to UUID
        private String jobTitle;
        private String companyName;
        private String employmentType;
        private String workMode;
        private LocalDateTime startDate;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return firstName != null ? firstName : email;
    }

    public Boolean isProfileComplete() {
        return profileCompleteness != null && profileCompleteness >= 80;
    }

    public Boolean isEmployed() {
        return currentEmploymentStatus != null &&
                !currentEmploymentStatus.equalsIgnoreCase("unemployed");
    }

    public String getEmploymentStatusDisplay() {
        if (currentEmploymentStatus != null && !currentEmploymentStatus.isBlank()) {
            return currentEmploymentStatus;
        }

        if (approvedJobApplications != null && approvedJobApplications > 0) {
            return "Previously Employed";
        }

        return "Seeking Opportunities";
    }
}