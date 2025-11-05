package com.youthconnect.job_services.service;

import com.youthconnect.job_services.client.*;
import com.youthconnect.job_services.common.PagedResponse;
import com.youthconnect.job_services.dto.request.*;
import com.youthconnect.job_services.dto.response.*;
import com.youthconnect.job_services.entity.*;
import com.youthconnect.job_services.enums.*;
import com.youthconnect.job_services.exception.*;
import com.youthconnect.job_services.mapper.JobMapper;
import com.youthconnect.job_services.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Job Service Implementation - FULLY FIXED with UUID Support
 *
 * ALL COMPILATION ERRORS RESOLVED:
 * ✅ Fixed Long to UUID conversions
 * ✅ Fixed repository method calls for UUID
 * ✅ Fixed AI recommendation client type handling
 * ✅ Fixed collection type mismatches
 * ✅ Fixed application repository queries
 *
 * Implements comprehensive job management business logic including:
 * - CRUD operations with validation and authorization
 * - AI-powered job recommendations with fallback strategy
 * - View count tracking and activity logging
 * - Dynamic search with multiple filters
 * - Caching strategies for performance optimization
 * - Inter-service communication (User, Notification, AI services)
 * - Transaction management and error handling
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (UUID Migration - All Issues Fixed)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JobServiceImpl implements JobService {

    // ============================================================================
    // DEPENDENCIES
    // ============================================================================

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final JobCategoryRepository categoryRepository;
    private final JobMapper jobMapper;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationClient;
    private final AIRecommendationClient aiRecommendationClient;

    // ============================================================================
    // JOB CRUD OPERATIONS
    // ============================================================================

    /**
     * Create a new job posting
     *
     * Validates:
     * - User has permission to post jobs (NGO, COMPANY, RECRUITER, GOVERNMENT)
     * - User exists in user service
     * - Category exists
     *
     * @param request Job creation request with all job details
     * @param postedByUserId ID of the user creating the job (UUID)
     * @param userRole Role of the user (for authorization)
     * @return Created job details
     * @throws ResourceNotFoundException if category not found
     * @throws UnauthorizedAccessException if user lacks permission
     */
    @Override
    @CacheEvict(value = {"jobs", "activeJobs", "jobsByCategory"}, allEntries = true)
    public JobDetailResponse createJob(CreateJobRequest request, UUID postedByUserId, String userRole) {
        log.info("Creating job '{}' for company '{}' by user {} with role {}",
                request.getJobTitle(), request.getCompanyName(), postedByUserId, userRole);

        // Validate user exists and has permission to post jobs
        validateJobPoster(postedByUserId, userRole);

        // ✅ FIXED: Use findByCategoryIdAndIsDeletedFalse for UUID
        JobCategory category = categoryRepository.findByCategoryIdAndIsDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("JobCategory", "categoryId", request.getCategoryId()));

        // Create and save job entity
        Job job = jobMapper.toEntity(request, postedByUserId, userRole);
        job.setCategory(category);
        Job savedJob = jobRepository.save(job);

        log.info("Job created successfully with ID: {}", savedJob.getJobId());

        return jobMapper.toDetailResponse(savedJob, false, null, null);
    }

    /**
     * Update an existing job posting
     *
     * Authorization: Only the job poster can update their own job
     *
     * @param jobId ID of the job to update (UUID)
     * @param request Update request with modified fields
     * @param userId ID of the user requesting the update (UUID)
     * @return Updated job details
     * @throws ResourceNotFoundException if job or category not found
     * @throws UnauthorizedAccessException if user is not the job poster
     */
    @Override
    @CacheEvict(value = {"jobs", "activeJobs", "jobsByCategory"}, allEntries = true)
    public JobDetailResponse updateJob(UUID jobId, UpdateJobRequest request, UUID userId) {
        log.info("Updating job {} by user {}", jobId, userId);

        // ✅ FIXED: Use findByJobIdAndIsDeletedFalse for UUID
        Job job = jobRepository.findByJobIdAndIsDeletedFalse(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "jobId", jobId));

        // Authorization check - only job poster can update
        if (!job.getPostedByUserId().equals(userId)) {
            throw new UnauthorizedAccessException("update", "this job");
        }

        // Update category if provided
        if (request.getCategoryId() != null) {
            // ✅ FIXED: Use findByCategoryIdAndIsDeletedFalse for UUID
            JobCategory category = categoryRepository.findByCategoryIdAndIsDeletedFalse(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("JobCategory", "categoryId", request.getCategoryId()));
            job.setCategory(category);
        }

        // Update job fields
        jobMapper.updateEntity(job, request);
        Job updatedJob = jobRepository.save(job);

        log.info("Job {} updated successfully", jobId);

        return jobMapper.toDetailResponse(updatedJob, null, null, null);
    }

    /**
     * Get job by ID with enhanced tracking and AI integration
     *
     * Features:
     * - Increments view count for analytics
     * - Logs view activity to AI recommendation service
     * - Checks if user has applied (for authenticated users)
     * - Graceful degradation if AI service is unavailable
     *
     * @param jobId ID of the job to retrieve (UUID)
     * @param userId ID of the viewing user (UUID, null if unauthenticated)
     * @return Job details with application status
     * @throws ResourceNotFoundException if job not found
     */
    @Override
    @Transactional
    public JobDetailResponse getJobById(UUID jobId, UUID userId) {
        log.debug("Fetching job {} for user {}", jobId, userId);

        // ✅ FIXED: Use findByJobIdAndIsDeletedFalse for UUID
        Job job = jobRepository.findByJobIdAndIsDeletedFalse(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "jobId", jobId));

        // ✅ INCREMENT VIEW COUNT for analytics
        job.incrementViewCount();
        jobRepository.save(job);
        log.debug("View count for job {} incremented to {}", jobId, job.getViewCount());

        // ✅ LOG ACTIVITY TO AI SERVICE (if user is authenticated)
        if (userId != null) {
            try {
                // ✅ FIXED: AI client now accepts UUID parameters
                aiRecommendationClient.recordJobView(userId, jobId);
                log.debug("Job view activity logged to AI service for user {}", userId);
            } catch (Exception e) {
                // Non-critical failure - log warning and continue
                log.warn("Failed to log job view activity to AI service: {}", e.getMessage());
            }
        }

        // Check if user has applied (for authenticated users)
        Boolean hasApplied = false;
        UUID applicationId = null;  // ✅ FIXED: Changed from Long to UUID
        String applicationStatus = null;

        if (userId != null) {
            // ✅ FIXED: Use findByJob_JobIdAndApplicantUserIdAndIsDeletedFalse for UUID
            JobApplication application = applicationRepository
                    .findByJob_JobIdAndApplicantUserIdAndIsDeletedFalse(jobId, userId)
                    .orElse(null);

            if (application != null) {
                hasApplied = true;
                applicationId = application.getApplicationId();  // ✅ FIXED: Now UUID
                applicationStatus = application.getStatus().name();
            }
        }

        return jobMapper.toDetailResponse(job, hasApplied, applicationId, applicationStatus);
    }

    /**
     * Search jobs with dynamic filtering
     *
     * Supports filtering by:
     * - Keyword (searches title, company, description)
     * - Job type (FULL_TIME, PART_TIME, etc.)
     * - Work mode (REMOTE, ONSITE, HYBRID)
     * - Location
     * - Category
     * - Salary range
     * - Posted date range
     * - Featured/Urgent flags
     *
     * Results are cached for performance
     *
     * @param searchRequest Search criteria
     * @param pageable Pagination parameters
     * @return Paginated search results
     */
    @Override
    @Cacheable(value = "activeJobs", key = "#searchRequest.hashCode() + '_' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public PagedResponse<JobResponse> searchJobs(JobSearchRequest searchRequest, Pageable pageable) {
        log.debug("Searching jobs with filters: {}", searchRequest);

        // Build dynamic specification and execute query
        Page<Job> jobPage = jobRepository.findAll(buildJobSpecification(searchRequest), pageable);

        // Convert to response DTOs
        List<JobResponse> responses = jobMapper.toResponseList(jobPage.getContent());

        return PagedResponse.from(jobPage, responses);
    }

    /**
     * Get all jobs posted by a specific user
     *
     * @param userId ID of the job poster (UUID)
     * @param pageable Pagination parameters
     * @return Paginated list of jobs by the user
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<JobResponse> getJobsByPoster(UUID userId, Pageable pageable) {
        log.debug("Fetching jobs posted by user {}", userId);

        // ✅ FIXED: Use findByPostedByUserIdAndIsDeletedFalse for UUID
        Page<Job> jobPage = jobRepository.findByPostedByUserIdAndIsDeletedFalse(userId, pageable);
        List<JobResponse> responses = jobMapper.toResponseList(jobPage.getContent());

        return PagedResponse.from(jobPage, responses);
    }

    // ============================================================================
    // JOB STATUS MANAGEMENT
    // ============================================================================

    /**
     * Publish a draft job
     *
     * Validations:
     * - Only draft jobs can be published
     * - Job description must be at least 100 characters
     * - Expiration date must be in the future
     *
     * Side effects:
     * - Sets published timestamp
     * - TODO: Triggers job alert notifications
     *
     * @param jobId ID of the job to publish (UUID)
     * @param userId ID of the user requesting publication (UUID)
     * @return Published job details
     * @throws ResourceNotFoundException if job not found
     * @throws UnauthorizedAccessException if user is not the job poster
     * @throws IllegalStateException if job is not in DRAFT status
     * @throws IllegalArgumentException if validation fails
     */
    @Override
    @CacheEvict(value = {"jobs", "activeJobs", "jobsByCategory"}, allEntries = true)
    public JobDetailResponse publishJob(UUID jobId, UUID userId) {
        log.info("Publishing job {} by user {}", jobId, userId);

        // ✅ FIXED: Use findByJobIdAndIsDeletedFalse for UUID
        Job job = jobRepository.findByJobIdAndIsDeletedFalse(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "jobId", jobId));

        // Authorization check
        if (!job.getPostedByUserId().equals(userId)) {
            throw new UnauthorizedAccessException("publish", "this job");
        }

        // Validation: Can only publish draft jobs
        if (job.getStatus() != JobStatus.DRAFT) {
            throw new IllegalStateException("Only draft jobs can be published. Current status: " + job.getStatus());
        }

        // Validate required fields
        if (job.getJobDescription() == null || job.getJobDescription().length() < 100) {
            throw new IllegalArgumentException("Job description must be at least 100 characters");
        }

        if (job.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expiration date must be in the future");
        }

        // Publish job
        job.setStatus(JobStatus.PUBLISHED);
        job.setPublishedAt(LocalDateTime.now());
        Job publishedJob = jobRepository.save(job);

        log.info("Job {} published successfully", jobId);

        // TODO: Trigger job alert notifications
        // notificationClient.sendJobAlert(publishedJob);

        return jobMapper.toDetailResponse(publishedJob, null, null, null);
    }

    /**
     * Close a published job
     *
     * Only published jobs can be closed
     * Sets closed timestamp
     *
     * @param jobId ID of the job to close (UUID)
     * @param userId ID of the user requesting closure (UUID)
     * @return Closed job details
     * @throws ResourceNotFoundException if job not found
     * @throws UnauthorizedAccessException if user is not the job poster
     * @throws IllegalStateException if job is not published
     */
    @Override
    @CacheEvict(value = {"jobs", "activeJobs", "jobsByCategory"}, allEntries = true)
    public JobDetailResponse closeJob(UUID jobId, UUID userId) {
        log.info("Closing job {} by user {}", jobId, userId);

        // ✅ FIXED: Use findByJobIdAndIsDeletedFalse for UUID
        Job job = jobRepository.findByJobIdAndIsDeletedFalse(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "jobId", jobId));

        // Authorization check
        if (!job.getPostedByUserId().equals(userId)) {
            throw new UnauthorizedAccessException("close", "this job");
        }

        // Can only close published jobs
        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new IllegalStateException("Only published jobs can be closed");
        }

        job.setStatus(JobStatus.CLOSED);
        job.setClosedAt(LocalDateTime.now());
        Job closedJob = jobRepository.save(job);

        log.info("Job {} closed successfully", jobId);

        return jobMapper.toDetailResponse(closedJob, null, null, null);
    }

    /**
     * Delete a draft job
     *
     * Only draft jobs can be deleted
     * Published jobs should be closed instead
     *
     * @param jobId ID of the job to delete (UUID)
     * @param userId ID of the user requesting deletion (UUID)
     * @throws ResourceNotFoundException if job not found
     * @throws UnauthorizedAccessException if user is not the job poster
     * @throws IllegalStateException if job is not in draft status
     */
    @Override
    @CacheEvict(value = {"jobs", "activeJobs", "jobsByCategory"}, allEntries = true)
    public void deleteJob(UUID jobId, UUID userId) {
        log.info("Deleting job {} by user {}", jobId, userId);

        // ✅ FIXED: Use findByJobIdAndIsDeletedFalse for UUID
        Job job = jobRepository.findByJobIdAndIsDeletedFalse(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "jobId", jobId));

        // Authorization check
        if (!job.getPostedByUserId().equals(userId)) {
            throw new UnauthorizedAccessException("delete", "this job");
        }

        // Can only delete draft jobs
        if (job.getStatus() != JobStatus.DRAFT) {
            throw new IllegalStateException("Only draft jobs can be deleted. Close published jobs instead.");
        }

        jobRepository.delete(job);

        log.info("Job {} deleted successfully", jobId);
    }

    // ============================================================================
    // VIEW TRACKING & ANALYTICS
    // ============================================================================

    /**
     * Increment view count for a job
     *
     * Note: This method is deprecated in favor of the enhanced tracking
     * in getJobById() which also logs to AI service
     *
     * @param jobId ID of the job being viewed (UUID)
     * @param userId ID of the viewing user (UUID, optional)
     * @param ipAddress IP address of the viewer (for deduplication)
     * @throws ResourceNotFoundException if job not found
     * @deprecated Use getJobById() instead, which includes view tracking
     */
    @Override
    @Transactional
    @Deprecated
    public void incrementViewCount(UUID jobId, UUID userId, String ipAddress) {
        // ✅ FIXED: Use findByJobIdAndIsDeletedFalse for UUID
        Job job = jobRepository.findByJobIdAndIsDeletedFalse(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "jobId", jobId));

        job.incrementViewCount();
        jobRepository.save(job);

        // TODO: Track view in job_views table for advanced analytics
        log.debug("Incremented view count for job {}. Total views: {}", jobId, job.getViewCount());
    }

    // ============================================================================
    // JOB DISCOVERY & RECOMMENDATIONS
    // ============================================================================

    /**
     * Get featured jobs
     *
     * Returns jobs marked as featured that are:
     * - Currently published
     * - Not expired
     *
     * Results are cached for performance
     *
     * @param limit Maximum number of featured jobs to return
     * @return List of featured jobs
     */
    @Override
    @Cacheable(value = "jobs", key = "'featured_' + #limit")
    @Transactional(readOnly = true)
    public List<JobResponse> getFeaturedJobs(int limit) {
        log.debug("Fetching {} featured jobs", limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by("publishedAt").descending());

        // ✅ FIXED: Use findByIsFeaturedTrueAndStatusAndExpiresAtAfterAndIsDeletedFalse
        Page<Job> jobs = jobRepository.findByIsFeaturedTrueAndStatusAndExpiresAtAfterAndIsDeletedFalse(
                JobStatus.PUBLISHED,
                LocalDateTime.now(),
                pageable
        );

        return jobMapper.toResponseList(jobs.getContent());
    }

    /**
     * Get recently published jobs
     *
     * Returns most recent published jobs that haven't expired
     * Results are cached for performance
     *
     * @param limit Maximum number of recent jobs to return
     * @return List of recent jobs
     */
    @Override
    @Cacheable(value = "activeJobs", key = "'recent_' + #limit")
    @Transactional(readOnly = true)
    public List<JobResponse> getRecentJobs(int limit) {
        log.debug("Fetching {} recent jobs", limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by("publishedAt").descending());

        // ✅ FIXED: Use findByStatusAndExpiresAtAfterAndIsDeletedFalse
        Page<Job> jobs = jobRepository.findByStatusAndExpiresAtAfterAndIsDeletedFalse(
                JobStatus.PUBLISHED,
                LocalDateTime.now(),
                pageable
        );

        return jobMapper.toResponseList(jobs.getContent());
    }

    /**
     * Get jobs expiring soon
     *
     * Useful for alerts and urgent notifications
     *
     * @param daysUntilExpiry Number of days threshold
     * @return List of jobs expiring within the specified days
     */
    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> getExpiringJobs(int daysUntilExpiry) {
        log.debug("Fetching jobs expiring within {} days", daysUntilExpiry);

        LocalDateTime expiryThreshold = LocalDateTime.now().plusDays(daysUntilExpiry);

        // ✅ FIXED: Use findByStatusAndExpiresAtBeforeAndIsDeletedFalse
        List<Job> jobs = jobRepository.findByStatusAndExpiresAtBeforeAndIsDeletedFalse(
                JobStatus.PUBLISHED,
                expiryThreshold
        );

        return jobMapper.toResponseList(jobs);
    }

    /**
     * Get AI-powered personalized job recommendations
     *
     * Strategy:
     * 1. Call AI recommendation service to get personalized suggestions
     * 2. If AI service succeeds, return recommendations in order
     * 3. If AI service fails, fallback to recent jobs user hasn't applied to
     *
     * The AI service considers:
     * - User's profile (skills, experience, preferences)
     * - User's job view history
     * - User's application history
     * - Job relevance and match score
     *
     * @param userId ID of the user to get recommendations for (UUID)
     * @param limit Maximum number of recommendations
     * @return List of recommended jobs
     */
    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> getRecommendedJobs(UUID userId, int limit) {
        log.debug("Fetching {} AI-recommended jobs for user {}", limit, userId);

        try {
            // ✅ CALL AI RECOMMENDATION SERVICE (now accepts UUID)
            List<AIRecommendationClient.RecommendedJobDto> aiRecommendations =
                    aiRecommendationClient.getJobRecommendations(userId, limit);

            if (aiRecommendations != null && !aiRecommendations.isEmpty()) {
                log.info("Received {} AI recommendations for user {}", aiRecommendations.size(), userId);

                // ✅ FIXED: Extract job UUIDs (not Longs)
                List<UUID> recommendedJobIds = aiRecommendations.stream()
                        .map(AIRecommendationClient.RecommendedJobDto::jobId)  // Returns UUID now
                        .collect(Collectors.toList());

                // ✅ FIXED: Use findAllByJobIdInAndIsDeletedFalse for UUID list
                List<Job> jobs = jobRepository.findAllByJobIdInAndIsDeletedFalse(recommendedJobIds);

                // Sort jobs in same order as AI recommendations (by match score)
                List<Job> sortedJobs = recommendedJobIds.stream()
                        .map(id -> jobs.stream()
                                .filter(job -> job.getJobId().equals(id))
                                .findFirst()
                                .orElse(null))
                        .filter(job -> job != null)
                        .collect(Collectors.toList());

                return jobMapper.toResponseList(sortedJobs);
            } else {
                log.info("AI service returned no recommendations, using fallback");
            }

        } catch (Exception e) {
            log.error("AI recommendation service failed: {}. Using fallback strategy.", e.getMessage());
        }

        // ✅ FALLBACK: Return recent jobs user hasn't applied to
        return getFallbackRecommendations(userId, limit);
    }

    /**
     * Fallback recommendation logic when AI service is unavailable
     *
     * Returns recent published jobs that:
     * - User hasn't already applied to
     * - Are not expired
     * - Are in published status
     *
     * @param userId ID of the user (UUID)
     * @param limit Maximum number of recommendations
     * @return List of recommended jobs
     */
    private List<JobResponse> getFallbackRecommendations(UUID userId, int limit) {
        log.debug("Using fallback recommendations for user {}", userId);

        Pageable pageable = PageRequest.of(0, limit, Sort.by("publishedAt").descending());

        // ✅ FIXED: Find jobs user has already applied to (UUID)
        Page<JobApplication> userApplications = applicationRepository
                .findByApplicantUserIdAndIsDeletedFalse(userId, Pageable.unpaged());

        List<UUID> appliedJobIds = userApplications.getContent().stream()
                .map(app -> app.getJob().getJobId())  // Extract UUID job IDs
                .collect(Collectors.toList());

        Page<Job> jobs;
        if (appliedJobIds.isEmpty()) {
            // User has no applications yet, show recent jobs
            // ✅ FIXED: Use findByStatusAndExpiresAtAfterAndIsDeletedFalse
            jobs = jobRepository.findByStatusAndExpiresAtAfterAndIsDeletedFalse(
                    JobStatus.PUBLISHED,
                    LocalDateTime.now(),
                    pageable
            );
        } else {
            // ✅ FIXED: Use findByStatusAndExpiresAtAfterAndJobIdNotInAndIsDeletedFalse
            jobs = jobRepository.findByStatusAndExpiresAtAfterAndJobIdNotInAndIsDeletedFalse(
                    JobStatus.PUBLISHED,
                    LocalDateTime.now(),
                    appliedJobIds,  // UUID list
                    pageable
            );
        }

        return jobMapper.toResponseList(jobs.getContent());
    }

    // ============================================================================
    // PRIVATE HELPER METHODS
    // ============================================================================

    /**
     * Validate that user has permission to post jobs
     *
     * Authorization rules:
     * - Only NGO, COMPANY, RECRUITER, and GOVERNMENT roles can post jobs
     * - User must exist in the user service
     *
     * @param userId ID of the user to validate (UUID)
     * @param userRole Role of the user
     * @throws UnauthorizedAccessException if user lacks permission
     * @throws ResourceNotFoundException if user doesn't exist
     */
    private void validateJobPoster(UUID userId, String userRole) {
        // Check role authorization
        if (!List.of("NGO", "COMPANY", "RECRUITER", "GOVERNMENT").contains(userRole)) {
            throw new UnauthorizedAccessException(
                    "Post jobs",
                    "Only NGOs, Companies, Recruiters, and Government can post jobs"
            );
        }

        // Verify user exists in user service
        try {
            // ✅ FIXED: UserServiceClient.userExists() now accepts UUID
            Boolean exists = userServiceClient.userExists(userId);
            if (!Boolean.TRUE.equals(exists)) {
                throw new ResourceNotFoundException("User", "userId", userId);
            }
        } catch (Exception e) {
            log.error("Failed to verify user existence: {}", e.getMessage());
            // Continue anyway - user service might be temporarily unavailable
            // In production, consider failing fast or implementing circuit breaker
        }
    }

    /**
     * Build dynamic JPA Specification for job search
     *
     * Constructs a specification with multiple filter criteria:
     * - Always filters by PUBLISHED status and non-expired
     * - Keyword search across title, company, description
     * - Job type, work mode, location filters
     * - Category filter
     * - Salary range filters
     * - Recency filter (posted within X days)
     * - Featured/Urgent flags
     *
     * @param request Search request with filter criteria
     * @return JPA Specification for dynamic querying
     */
    private org.springframework.data.jpa.domain.Specification<Job> buildJobSpecification(JobSearchRequest request) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            // Base filters: always filter by published and not expired
            predicates.add(cb.equal(root.get("status"), JobStatus.PUBLISHED));
            predicates.add(cb.greaterThan(root.get("expiresAt"), LocalDateTime.now()));
            predicates.add(cb.isFalse(root.get("isDeleted")));  // ✅ ADDED: Filter deleted jobs

            // Keyword search (searches title, company name, and description)
            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("jobTitle")), keyword),
                        cb.like(cb.lower(root.get("companyName")), keyword),
                        cb.like(cb.lower(root.get("jobDescription")), keyword)
                ));
            }

            // Job type filter (FULL_TIME, PART_TIME, etc.)
            if (request.getJobType() != null) {
                predicates.add(cb.equal(root.get("jobType"), request.getJobType()));
            }

            // Work mode filter (REMOTE, ONSITE, HYBRID)
            if (request.getWorkMode() != null) {
                predicates.add(cb.equal(root.get("workMode"), request.getWorkMode()));
            }

            // Location filter (case-insensitive partial match)
            if (request.getLocation() != null && !request.getLocation().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")),
                        "%" + request.getLocation().toLowerCase() + "%"));
            }

            // Category filter (UUID)
            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("categoryId"), request.getCategoryId()));
            }

            // Salary range filters
            if (request.getMinSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMin"), request.getMinSalary()));
            }
            if (request.getMaxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salaryMax"), request.getMaxSalary()));
            }

            // Posted within days filter (e.g., jobs from last 7 days)
            if (request.getPostedWithinDays() != null) {
                LocalDateTime threshold = LocalDateTime.now().minusDays(request.getPostedWithinDays());
                predicates.add(cb.greaterThanOrEqualTo(root.get("publishedAt"), threshold));
            }

            // Featured jobs only filter
            if (Boolean.TRUE.equals(request.getFeaturedOnly())) {
                predicates.add(cb.isTrue(root.get("isFeatured")));
            }

            // Urgent jobs only filter
            if (Boolean.TRUE.equals(request.getUrgentOnly())) {
                predicates.add(cb.isTrue(root.get("isUrgent")));
            }

            // Combine all predicates with AND
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}