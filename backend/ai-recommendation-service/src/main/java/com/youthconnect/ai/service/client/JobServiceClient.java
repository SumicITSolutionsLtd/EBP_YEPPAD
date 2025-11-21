package com.youthconnect.ai.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Job Service Client for AI Recommendation Service
 *
 * Allows AI service to fetch job data for generating recommendations.
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@FeignClient(
        name = "job-service",
        path = "/api/v1",
        fallbackFactory = JobServiceClientFallback.class
)
public interface JobServiceClient {

    /**
     * Get job details by ID for recommendation scoring
     */
    @GetMapping("/jobs/{jobId}")
    JobResponse getJobById(@PathVariable("jobId") Long jobId);

    /**
     * Get recent jobs for recommendation pool
     */
    @GetMapping("/jobs/recent")
    List<JobResponse> getRecentJobs(@RequestParam(defaultValue = "50") int limit);

    /**
     * Search jobs with filters for matching
     */
    @GetMapping("/jobs/search")
    JobSearchResponse searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String workMode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    /**
     * Get jobs by category for content-based filtering
     */
    @GetMapping("/categories/{categoryId}/jobs")
    List<JobResponse> getJobsByCategory(@PathVariable Long categoryId);

    /**
     * Get job statistics for recommendation scoring
     */
    @GetMapping("/jobs/{jobId}/stats")
    JobStatsResponse getJobStats(@PathVariable Long jobId);

    // =========================================================================
    // DTOs
    // =========================================================================

    record JobResponse(
            Long jobId,
            String jobTitle,
            String companyName,
            String jobType,
            String workMode,
            String location,
            Long categoryId,
            String categoryName,
            Integer applicationCount,
            Integer viewCount,
            Boolean isFeatured,
            String publishedAt,
            String expiresAt
    ) {}

    record JobSearchResponse(
            List<JobResponse> content,
            int totalPages,
            long totalElements,
            int pageNumber
    ) {}

    record JobStatsResponse(
            Long jobId,
            Integer totalApplications,
            Integer totalViews,
            Double averageApplicationTime,
            Integer successfulPlacements
    ) {}
}
