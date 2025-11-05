package com.youthconnect.job_services.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User Service Client
 *
 * Feign client for communication with user-service from job-service.
 * Used to verify user existence and retrieve user profile information.
 *
 * @author Douglas Kings Kato
 * @since 1.0.0
 */
@FeignClient(
        name = "user-service",
        path = "/api/v1/users",
        fallbackFactory = UserServiceClientFallback.class
)
public interface UserServiceClient {

    /**
     * Check if a user exists by user ID
     *
     * This is used to validate job poster credentials before creating a job.
     * Returns true if user exists and is active.
     *
     * @param userId The user ID to check
     * @return Boolean indicating if user exists
     */
    @GetMapping("/{userId}/exists")
    Boolean userExists(@PathVariable("userId") UUID userId);

    /**
     * Get user profile summary
     *
     * Used to display job poster information on job listings.
     *
     * @param userId The user ID
     * @return UserProfileResponse with basic user info
     */
    @GetMapping("/{userId}/summary")
    UserProfileResponse getUserSummary(@PathVariable("userId") UUID userId);

    /**
     * Verify user has permission to post jobs
     *
     * Checks if user role allows job posting (NGO, COMPANY, RECRUITER, GOVERNMENT)
     *
     * @param userId The user ID to verify
     * @return Boolean indicating if user can post jobs
     */
    @GetMapping("/{userId}/can-post-jobs")
    Boolean canUserPostJobs(@PathVariable("userId") UUID userId);

    /**
     * Get user's organization name
     *
     * Used to auto-fill company name when creating jobs.
     *
     * @param userId The user ID
     * @return Organization name or null
     */
    @GetMapping("/{userId}/organization")
    String getUserOrganization(@PathVariable("userId") UUID  userId);

    // =========================================================================
    // DTOs (Data Transfer Objects)
    // =========================================================================

    /**
     * User Profile Summary Response
     * Contains basic user information needed for job service
     */
    record UserProfileResponse(
            UUID  userId,
            String email,
            String fullName,
            String role,
            String organizationName,
            Boolean isActive,
            Boolean isVerified
    ) {}
}
