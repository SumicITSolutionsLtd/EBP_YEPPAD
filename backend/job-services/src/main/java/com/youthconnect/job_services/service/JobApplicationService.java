package com.youthconnect.job_services.service;

import com.youthconnect.job_services.dto.request.CreateApplicationRequest;
import com.youthconnect.job_services.dto.response.ApplicationResponse;
import com.youthconnect.job_services.common.PagedResponse;
import com.youthconnect.job_services.enums.ApplicationStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Job Application Service Interface - FIXED with UUID
 *
 * ✅ ALL METHODS NOW USE UUID INSTEAD OF LONG
 *
 * Defines contract for job application business logic operations.
 * All identifiers use UUID following platform guidelines.
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (UUID Migration - FIXED)
 * @since 1.0.0
 */
public interface JobApplicationService {

    /**
     * Submit a job application
     *
     * ✅ FIXED: applicantUserId changed from Long to UUID
     *
     * @param request Application details
     * @param applicantUserId UUID of the applicant
     * @return Created application response
     */
    ApplicationResponse submitApplication(CreateApplicationRequest request, UUID applicantUserId);

    /**
     * Get application by ID
     *
     * ✅ FIXED: applicationId and userId changed from Long to UUID
     *
     * @param applicationId Application UUID
     * @param userId User UUID requesting the application
     * @return Application details
     */
    ApplicationResponse getApplicationById(UUID applicationId, UUID userId);

    /**
     * Get all applications for a job (job poster only)
     *
     * ✅ FIXED: jobId and userId changed from Long to UUID
     *
     * @param jobId Job UUID
     * @param userId User UUID (must be job poster)
     * @param pageable Pagination parameters
     * @return Paginated list of applications
     */
    PagedResponse<ApplicationResponse> getApplicationsByJob(UUID jobId, UUID userId, Pageable pageable);

    /**
     * Get user's applications
     *
     * ✅ FIXED: userId changed from Long to UUID
     *
     * @param userId User UUID
     * @param pageable Pagination parameters
     * @return Paginated list of user's applications
     */
    PagedResponse<ApplicationResponse> getMyApplications(UUID userId, Pageable pageable);

    /**
     * Update application status (job poster only)
     *
     * ✅ FIXED: applicationId and reviewerId changed from Long to UUID
     *
     * @param applicationId Application UUID
     * @param newStatus New application status
     * @param reviewNotes Review notes (optional)
     * @param reviewerId UUID of user reviewing the application
     * @return Updated application
     */
    ApplicationResponse updateApplicationStatus(
            UUID applicationId,
            ApplicationStatus newStatus,
            String reviewNotes,
            UUID reviewerId
    );

    /**
     * Withdraw application (applicant only)
     *
     * ✅ FIXED: applicationId and userId changed from Long to UUID
     *
     * @param applicationId Application UUID
     * @param userId User UUID (must be applicant)
     */
    void withdrawApplication(UUID applicationId, UUID userId);
}