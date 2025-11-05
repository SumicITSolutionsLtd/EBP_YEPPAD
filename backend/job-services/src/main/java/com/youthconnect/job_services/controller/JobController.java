package com.youthconnect.job_services.controller;

import com.youthconnect.job_services.common.ApiResponse;
import com.youthconnect.job_services.common.PagedResponse;
import com.youthconnect.job_services.dto.request.*;
import com.youthconnect.job_services.dto.response.*;
import com.youthconnect.job_services.security.GatewayUserContextUtil;
import com.youthconnect.job_services.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Job Controller - UPDATED for API Gateway Authentication
 *
 * AUTHENTICATION CHANGES (Version 3.0):
 * ❌ REMOVED: @RequestHeader("X-User-Id") parameters
 * ❌ REMOVED: Direct header extraction
 * ✅ ADDED: GatewayUserContextUtil for user context
 * ✅ ADDED: Automatic user context extraction from gateway headers
 *
 * Endpoints remain the same, but authentication is now handled transparently
 * through the gateway. User ID and role are extracted from headers added by
 * the API Gateway after JWT validation.
 *
 * EXAMPLE FLOW:
 * 1. Client → POST /api/v1/jobs (with JWT in Authorization header)
 * 2. API Gateway validates JWT
 * 3. Gateway adds X-User-Id and X-User-Role headers
 * 4. Request forwarded to this controller
 * 5. GatewayUserContextUtil extracts user context
 * 6. Business logic proceeds with authenticated user
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (Updated for Gateway Authentication)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job posting and management endpoints")
public class JobController {

    private final JobService jobService;

    /**
     * Create a new job posting
     *
     * ⚠️ UPDATED: Now uses GatewayUserContextUtil instead of @RequestHeader
     *
     * Authorization:
     * - Only NGO, COMPANY, RECRUITER, GOVERNMENT roles can post jobs
     * - User must be authenticated via API Gateway
     *
     * @param request Job creation request
     * @return Created job details
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create job",
            description = "Create a new job posting (NGO, Company, Recruiter, Government only)"
    )
    public ApiResponse<JobDetailResponse> createJob(@Valid @RequestBody CreateJobRequest request) {
        // Extract user context from gateway headers
        UUID userId = GatewayUserContextUtil.getCurrentUserId();
        String userRole = GatewayUserContextUtil.getCurrentUserRole();

        log.info("Creating job for user: {} with role: {}", userId, userRole);

        JobDetailResponse job = jobService.createJob(request, userId, userRole);
        return ApiResponse.success("Job created successfully", job);
    }

    /**
     * Update existing job
     *
     * ⚠️ UPDATED: Now uses GatewayUserContextUtil
     */
    @PutMapping("/{jobId}")
    @Operation(summary = "Update job", description = "Update job details (owner only)")
    public ApiResponse<JobDetailResponse> updateJob(
            @PathVariable UUID jobId,
            @Valid @RequestBody UpdateJobRequest request
    ) {
        UUID userId = GatewayUserContextUtil.getCurrentUserId();

        log.info("User {} updating job {}", userId, jobId);

        JobDetailResponse job = jobService.updateJob(jobId, request, userId);
        return ApiResponse.success("Job updated successfully", job);
    }

    /**
     * Get job details by ID
     *
     * ⚠️ UPDATED: User ID is now optional (for anonymous viewing)
     *
     * @param jobId Job UUID
     * @return Job details with application status (if authenticated)
     */
    @GetMapping("/{jobId}")
    @Operation(summary = "Get job details", description = "Get detailed job information")
    public ApiResponse<JobDetailResponse> getJobById(@PathVariable UUID jobId) {
        // User ID is optional - anonymous users can view jobs
        UUID userId = GatewayUserContextUtil.getCurrentUserId();

        log.debug("User {} viewing job {}", userId != null ? userId : "anonymous", jobId);

        JobDetailResponse job = jobService.getJobById(jobId, userId);
        return ApiResponse.success(job);
    }

    /**
     * Search and filter jobs
     *
     * PUBLIC ENDPOINT - No authentication required
     */
    @GetMapping("/search")
    @Operation(summary = "Search jobs", description = "Search and filter jobs with pagination")
    public ApiResponse<PagedResponse<JobResponse>> searchJobs(
            @ModelAttribute JobSearchRequest searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<JobResponse> jobs = jobService.searchJobs(searchRequest, pageable);
        return ApiResponse.success(jobs);
    }

    /**
     * Get jobs posted by authenticated user
     *
     * ⚠️ UPDATED: Now uses GatewayUserContextUtil
     */
    @GetMapping("/my-jobs")
    @Operation(summary = "Get my posted jobs", description = "Get all jobs posted by the authenticated user")
    public ApiResponse<PagedResponse<JobResponse>> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = GatewayUserContextUtil.getCurrentUserId();
        GatewayUserContextUtil.requireUserContext(); // Ensure authenticated

        log.info("Fetching jobs for user: {}", userId);

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<JobResponse> jobs = jobService.getJobsByPoster(userId, pageable);
        return ApiResponse.success(jobs);
    }

    /**
     * Publish a draft job
     *
     * ⚠️ UPDATED: Now uses GatewayUserContextUtil
     */
    @PutMapping("/{jobId}/publish")
    @Operation(summary = "Publish job", description = "Publish a draft job to make it visible")
    public ApiResponse<JobDetailResponse> publishJob(@PathVariable UUID jobId) {
        UUID userId = GatewayUserContextUtil.getCurrentUserId();

        log.info("User {} publishing job {}", userId, jobId);

        JobDetailResponse job = jobService.publishJob(jobId, userId);
        return ApiResponse.success("Job published successfully", job);
    }

    /**
     * Close a job (stop accepting applications)
     *
     * ⚠️ UPDATED: Now uses GatewayUserContextUtil
     */
    @PutMapping("/{jobId}/close")
    @Operation(summary = "Close job", description = "Close job and stop accepting applications")
    public ApiResponse<JobDetailResponse> closeJob(@PathVariable UUID jobId) {
        UUID userId = GatewayUserContextUtil.getCurrentUserId();

        log.info("User {} closing job {}", userId, jobId);

        JobDetailResponse job = jobService.closeJob(jobId, userId);
        return ApiResponse.success("Job closed successfully", job);
    }

    /**
     * Delete a job
     *
     * ⚠️ UPDATED: Now uses GatewayUserContextUtil
     */
    @DeleteMapping("/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete job", description = "Delete a draft job")
    public void deleteJob(@PathVariable UUID jobId) {
        UUID userId = GatewayUserContextUtil.getCurrentUserId();

        log.info("User {} deleting job {}", userId, jobId);

        jobService.deleteJob(jobId, userId);
    }

    /**
     * Get featured jobs
     *
     * PUBLIC ENDPOINT - No authentication required
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured jobs", description = "Get featured jobs for homepage")
    public ApiResponse<List<JobResponse>> getFeaturedJobs(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<JobResponse> jobs = jobService.getFeaturedJobs(limit);
        return ApiResponse.success(jobs);
    }

    /**
     * Get recent jobs
     *
     * PUBLIC ENDPOINT - No authentication required
     */
    @GetMapping("/recent")
    @Operation(summary = "Get recent jobs", description = "Get recently posted jobs")
    public ApiResponse<List<JobResponse>> getRecentJobs(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<JobResponse> jobs = jobService.getRecentJobs(limit);
        return ApiResponse.success(jobs);
    }

    /**
     * Get recommended jobs for user
     *
     * ⚠️ UPDATED: Now uses GatewayUserContextUtil
     *
     * Uses AI-powered recommendations based on:
     * - User profile (skills, interests)
     * - Job view history
     * - Application history
     * - Similar users' behavior
     */
    @GetMapping("/recommended")
    @Operation(summary = "Get recommended jobs", description = "Get AI-powered job recommendations")
    public ApiResponse<List<JobResponse>> getRecommendedJobs(
            @RequestParam(defaultValue = "10") int limit
    ) {
        UUID userId = GatewayUserContextUtil.getCurrentUserId();
        GatewayUserContextUtil.requireUserContext(); // Ensure authenticated

        log.info("Fetching {} recommended jobs for user: {}", limit, userId);

        List<JobResponse> jobs = jobService.getRecommendedJobs(userId, limit);
        return ApiResponse.success(jobs);
    }
}