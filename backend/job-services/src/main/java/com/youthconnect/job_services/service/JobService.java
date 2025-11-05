package com.youthconnect.job_services.service;

import com.youthconnect.job_services.dto.request.*;
import com.youthconnect.job_services.dto.response.*;
import com.youthconnect.job_services.common.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Job Service Interface - FIXED with UUID Support
 *
 * All ID parameters updated to UUID.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
public interface JobService {

    /**
     * Create a new job posting
     * FIXED: userId changed to UUID
     */
    JobDetailResponse createJob(CreateJobRequest request, UUID postedByUserId, String userRole);

    /**
     * Update an existing job
     * FIXED: jobId and userId changed to UUID
     */
    JobDetailResponse updateJob(UUID jobId, UpdateJobRequest request, UUID userId);

    /**
     * Get job by ID with full details
     * FIXED: jobId and userId changed to UUID
     */
    JobDetailResponse getJobById(UUID jobId, UUID userId);

    /**
     * Search/filter jobs with pagination
     */
    PagedResponse<JobResponse> searchJobs(JobSearchRequest searchRequest, Pageable pageable);

    /**
     * Get jobs posted by a specific user
     * FIXED: userId changed to UUID
     */
    PagedResponse<JobResponse> getJobsByPoster(UUID userId, Pageable pageable);

    /**
     * Publish a draft job
     * FIXED: jobId and userId changed to UUID
     */
    JobDetailResponse publishJob(UUID jobId, UUID userId);

    /**
     * Close a job (stop accepting applications)
     * FIXED: jobId and userId changed to UUID
     */
    JobDetailResponse closeJob(UUID jobId, UUID userId);

    /**
     * Delete a job (only draft jobs can be deleted)
     * FIXED: jobId and userId changed to UUID
     */
    void deleteJob(UUID jobId, UUID userId);

    /**
     * Increment view count when someone views a job
     * FIXED: jobId and userId changed to UUID
     */
    void incrementViewCount(UUID jobId, UUID userId, String ipAddress);

    /**
     * Get featured jobs
     */
    List<JobResponse> getFeaturedJobs(int limit);

    /**
     * Get recently posted jobs
     */
    List<JobResponse> getRecentJobs(int limit);

    /**
     * Get jobs expiring soon
     */
    List<JobResponse> getExpiringJobs(int daysUntilExpiry);

    /**
     * Get recommended jobs for a user
     * FIXED: userId changed to UUID
     */
    List<JobResponse> getRecommendedJobs(UUID userId, int limit);
}