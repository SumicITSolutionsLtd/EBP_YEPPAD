package com.youthconnect.job_services.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Job Service Client
 *
 * Feign client for communication with job-service from user-service.
 * Used to fetch user's job applications and employment history.
 */
@FeignClient(name = "job-service", path = "/api/v1")
public interface JobServiceClient {

    /**
     * Get user's job applications summary
     */
    @GetMapping("/applications/user/{userId}/summary")
    ApplicationSummaryResponse getUserApplicationSummary(@PathVariable Long userId);

    /**
     * Get user's active applications count
     */
    @GetMapping("/applications/user/{userId}/count")
    Integer getUserActiveApplicationsCount(@PathVariable Long userId);

    /**
     * Check if user has applied to a specific job
     */
    @GetMapping("/applications/user/{userId}/job/{jobId}/exists")
    Boolean hasUserAppliedToJob(
            @PathVariable Long userId,
            @PathVariable Long jobId
    );

    /**
     * DTOs for Job Service responses
     */
    record ApplicationSummaryResponse(
            Integer totalApplications,
            Integer pendingApplications,
            Integer approvedApplications,
            Integer rejectedApplications
    ) {}
}