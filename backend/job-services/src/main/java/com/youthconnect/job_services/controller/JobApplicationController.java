package com.youthconnect.job_services.controller;

import com.youthconnect.job_services.common.ApiResponse;
import com.youthconnect.job_services.common.PagedResponse;
import com.youthconnect.job_services.dto.request.CreateApplicationRequest;
import com.youthconnect.job_services.dto.response.ApplicationResponse;
import com.youthconnect.job_services.enums.ApplicationStatus;
import com.youthconnect.job_services.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Job Application Controller - UUID Version
 *
 * UPDATED for Backend Guidelines:
 * - All IDs use UUID
 * - User context from API Gateway headers (X-User-Id)
 * - Pagination for all list endpoints
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Job Applications", description = "Job application management endpoints")
public class JobApplicationController {

    private final JobApplicationService applicationService;

    /**
     * Submit a job application
     * User ID extracted from JWT by API Gateway
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit application", description = "Submit an application to a job posting")
    public ApiResponse<ApplicationResponse> submitApplication(
            @Valid @RequestBody CreateApplicationRequest request,
            @RequestHeader("X-User-Id") UUID userId  // From API Gateway
    ) {
        ApplicationResponse application = applicationService.submitApplication(request, userId);
        return ApiResponse.success("Application submitted successfully", application);
    }

    /**
     * Get application details by UUID
     */
    @GetMapping("/{applicationId}")
    @Operation(summary = "Get application", description = "Get application details by ID")
    public ApiResponse<ApplicationResponse> getApplication(
            @PathVariable UUID applicationId,  // UUID
            @RequestHeader("X-User-Id") UUID userId
    ) {
        ApplicationResponse application = applicationService.getApplicationById(applicationId, userId);
        return ApiResponse.success(application);
    }

    /**
     * Get all applications for a job (job poster only)
     * Returns paginated results
     */
    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get job applications", description = "Get all applications for a job (job poster only)")
    public ApiResponse<PagedResponse<ApplicationResponse>> getJobApplications(
            @PathVariable UUID jobId,  // UUID
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ApplicationResponse> applications = applicationService.getApplicationsByJob(jobId, userId, pageable);
        return ApiResponse.success(applications);
    }

    /**
     * Get user's applications
     * Returns paginated results
     */
    @GetMapping("/my-applications")
    @Operation(summary = "Get my applications", description = "Get all applications submitted by the authenticated user")
    public ApiResponse<PagedResponse<ApplicationResponse>> getMyApplications(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ApplicationResponse> applications = applicationService.getMyApplications(userId, pageable);
        return ApiResponse.success(applications);
    }

    /**
     * Update application status (job poster only)
     */
    @PutMapping("/{applicationId}/status")
    @Operation(summary = "Update application status", description = "Review and update application status (job poster only)")
    public ApiResponse<ApplicationResponse> updateApplicationStatus(
            @PathVariable UUID applicationId,  // UUID
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String reviewNotes,
            @RequestHeader("X-User-Id") UUID reviewerId
    ) {
        ApplicationResponse application = applicationService.updateApplicationStatus(
                applicationId, status, reviewNotes, reviewerId);
        return ApiResponse.success("Application status updated successfully", application);
    }

    /**
     * Withdraw application (applicant only)
     */
    @DeleteMapping("/{applicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Withdraw application", description = "Withdraw a submitted application")
    public void withdrawApplication(
            @PathVariable UUID applicationId,  // UUID
            @RequestHeader("X-User-Id") UUID userId
    ) {
        applicationService.withdrawApplication(applicationId, userId);
    }
}