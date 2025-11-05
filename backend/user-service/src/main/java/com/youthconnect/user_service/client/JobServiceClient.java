package com.youthconnect.user_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * JOB SERVICE CLIENT - FEIGN CLIENT FOR USER SERVICE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * This Feign client enables user-service to communicate with job-service
 * to fetch job application statistics and employment history for user profiles.
 *
 * Usage:
 * - Called when building comprehensive user profiles
 * - Used in dashboard endpoints to show application stats
 * - Integration point for employment history tracking
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-20
 */
@FeignClient(
        name = "job-service",           // Service name in Eureka registry
        path = "/api/v1",                // Base path for all endpoints
        fallback = JobServiceClientFallback.class  // Fallback for resilience
)
public interface JobServiceClient {

    /**
     * Get user's job applications summary
     *
     * Returns aggregated statistics about a user's job applications including:
     * - Total number of applications submitted
     * - Applications currently pending review
     * - Applications that were approved/accepted
     * - Applications that were rejected
     *
     * Used by: User profile endpoints to display application stats
     *
     * @param userId The ID of the user
     * @return ApplicationSummaryResponse with aggregated statistics
     */
    @GetMapping("/applications/user/{userId}/summary")
    ApplicationSummaryResponse getUserApplicationSummary(@PathVariable("userId") UUID userId);

    /**
     * Get count of user's active applications
     *
     * Returns only the count of applications that are currently "active"
     * (PENDING, UNDER_REVIEW, SHORTLISTED, or INTERVIEW_SCHEDULED status).
     *
     * Used by: User dashboards to show quick stats
     *
     * @param userId The ID of the user
     * @return Integer count of active applications
     */
    @GetMapping("/applications/user/{userId}/count")
    Integer getUserActiveApplicationsCount(@PathVariable("userId") UUID userId);

    /**
     * Check if user has already applied to a specific job
     *
     * Used to prevent duplicate applications and show "Already Applied" badges
     * in the UI when users browse jobs.
     *
     * @param userId The ID of the user
     * @param jobId The ID of the job
     * @return Boolean - true if user has applied, false otherwise
     */
    @GetMapping("/applications/user/{userId}/job/{jobId}/exists")
    Boolean hasUserAppliedToJob(
            @PathVariable("userId") UUID userId,
            @PathVariable("jobId") Long jobId
    );

    /**
     * Get user's recent job applications
     *
     * Returns the latest job applications submitted by the user,
     * sorted by submission date (most recent first).
     *
     * @param userId The ID of the user
     * @param limit Maximum number of applications to return
     * @return List of recent job applications
     */
    @GetMapping("/applications/user/{userId}/recent")
    java.util.List<RecentApplicationDto> getUserRecentApplications(
            @PathVariable("userId") UUID userId,
            @RequestParam(defaultValue = "5") int limit
    );

    /**
     * Get user's current employment status from latest approved application
     *
     * Checks if user has any ACCEPTED job applications and returns
     * the most recent one to determine current employment status.
     *
     * @param userId The ID of the user
     * @return CurrentEmploymentDto with employment details, or null if unemployed
     */
    @GetMapping("/applications/user/{userId}/current-employment")
    CurrentEmploymentDto getCurrentEmployment(@PathVariable("userId") UUID userId);

    // =========================================================================
    // DATA TRANSFER OBJECTS (DTOs)
    // =========================================================================

    /**
     * Application Summary Response DTO
     *
     * Aggregated statistics about a user's job applications.
     */
    record ApplicationSummaryResponse(
            Integer totalApplications,      // Total applications ever submitted
            Integer pendingApplications,    // Applications awaiting review
            Integer approvedApplications,   // Applications accepted/hired
            Integer rejectedApplications,   // Applications rejected
            Integer withdrawnApplications,  // Applications withdrawn by user
            Double successRate             // Approval rate (approved/total * 100)
    ) {}

    /**
     * Recent Application DTO
     *
     * Summary of a recent job application for display in user profile.
     */
    record RecentApplicationDto(
            Long applicationId,
            Long jobId,
            String jobTitle,
            String companyName,
            String status,              // PENDING, APPROVED, REJECTED, etc.
            java.time.LocalDateTime submittedAt
    ) {}

    /**
     * Current Employment DTO
     *
     * Details about user's current employment from latest approved application.
     */
    record CurrentEmploymentDto(
            Long jobId,
            String jobTitle,
            String companyName,
            String employmentType,      // FULL_TIME, PART_TIME, CONTRACT
            java.time.LocalDateTime startDate,
            Boolean isCurrent           // true if still employed
    ) {}
}