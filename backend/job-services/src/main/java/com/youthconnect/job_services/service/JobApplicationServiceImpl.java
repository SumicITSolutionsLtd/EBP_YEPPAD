package com.youthconnect.job_services.service;

import com.youthconnect.job_services.client.*;
import com.youthconnect.job_services.common.PagedResponse;
import com.youthconnect.job_services.dto.request.CreateApplicationRequest;
import com.youthconnect.job_services.dto.response.ApplicationResponse;
import com.youthconnect.job_services.entity.*;
import com.youthconnect.job_services.enums.*;
import com.youthconnect.job_services.exception.*;
import com.youthconnect.job_services.mapper.JobMapper;
import com.youthconnect.job_services.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Job Application Service Implementation - UUID Version
 *
 * UPDATED for Backend Guidelines:
 * - All IDs use UUID instead of Long
 * - Pagination for all list methods
 * - No collection returns without pagination
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final JobMapper jobMapper;
    private final NotificationServiceClient notificationClient;

    /**
     * Submit a job application
     *
     * @param request Application details
     * @param applicantUserId UUID of the applicant
     * @return Created application response
     */
    @Override
    public ApplicationResponse submitApplication(CreateApplicationRequest request, UUID applicantUserId) {
        log.info("User {} applying to job {}", applicantUserId, request.getJobId());

        // Fetch job with UUID
        Job job = jobRepository.findByJobIdAndIsDeletedFalse(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", "jobId", request.getJobId()));

        // Validate job accepts applications
        validateJobAcceptsApplications(job);

        // Check for duplicate application (UUID)
        if (applicationRepository.existsByJob_JobIdAndApplicantUserIdAndIsDeletedFalse(
                request.getJobId(), applicantUserId)) {
            throw new DuplicateApplicationException(
                    String.format("User %s has already applied to job %s", applicantUserId, request.getJobId())
            );
        }

        // Create application entity
        JobApplication application = JobApplication.builder()
                .job(job)
                .applicantUserId(applicantUserId)
                .coverLetter(request.getCoverLetter())
                .resumeFileId(request.getResumeFileId())
                .status(ApplicationStatus.SUBMITTED)
                .build();

        JobApplication saved = applicationRepository.save(application);

        // Increment job application count
        job.incrementApplicationCount();
        jobRepository.save(job);

        // Send notifications
        sendApplicationNotifications(job, applicantUserId);

        log.info("Application submitted successfully. ID: {}", saved.getApplicationId());

        return jobMapper.toApplicationResponse(saved);
    }

    /**
     * Validates if a job can accept applications
     */
    private void validateJobAcceptsApplications(Job job) {
        if (!job.isAcceptingApplications()) {
            if (job.isExpired()) {
                throw new JobExpiredException(
                        job.getJobId(),
                        job.getJobTitle(),
                        job.getExpiresAt()
                );
            }
            if (job.hasReachedMaxApplications()) {
                throw new MaxApplicationsReachedException(
                        job.getJobId(),
                        job.getJobTitle(),
                        job.getApplicationCount(),
                        job.getMaxApplications()
                );
            }
            throw new InvalidJobStatusException(
                    job.getJobId(),
                    "apply",
                    job.getStatus()
            );
        }
    }

    /**
     * Send notifications for new application
     */
    private void sendApplicationNotifications(Job job, UUID applicantUserId) {
        try {
            // Notify applicant
            notificationClient.sendApplicationConfirmation(
                    applicantUserId,
                    new NotificationServiceClient.JobApplicationNotification(
                            job.getJobId(),
                            job.getJobTitle(),
                            job.getCompanyName(),
                            null,
                            null
                    )
            );

            // Notify job poster
            notificationClient.sendNewApplicationAlert(
                    job.getPostedByUserId(),  // UUID
                    new NotificationServiceClient.JobApplicationNotification(
                            job.getJobId(),
                            job.getJobTitle(),
                            job.getCompanyName(),
                            null,
                            null
                    )
            );
        } catch (Exception e) {
            log.error("Failed to send application notifications: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(UUID applicationId, UUID userId) {
        JobApplication application = applicationRepository.findByApplicationIdAndIsDeletedFalse(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "applicationId", applicationId));

        // Authorization: Only applicant or job poster can view
        if (!application.getApplicantUserId().equals(userId) &&
                !application.getJob().getPostedByUserId().equals(userId)) {
            throw new UnauthorizedAccessException("view", "this application");
        }

        return jobMapper.toApplicationResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ApplicationResponse> getApplicationsByJob(UUID jobId, UUID userId, Pageable pageable) {
        // Verify job exists and user is the poster
        Job job = jobRepository.findByJobIdAndIsDeletedFalse(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "jobId", jobId));

        if (!job.getPostedByUserId().equals(userId)) {
            throw new UnauthorizedAccessException("view applications for", "this job");
        }

        Page<JobApplication> applications = applicationRepository.findByJob_JobIdAndIsDeletedFalse(jobId, pageable);

        return PagedResponse.from(applications,
                applications.getContent().stream()
                        .map(jobMapper::toApplicationResponse)
                        .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ApplicationResponse> getMyApplications(UUID userId, Pageable pageable) {
        Page<JobApplication> applications = applicationRepository.findByApplicantUserIdAndIsDeletedFalse(userId, pageable);

        return PagedResponse.from(applications,
                applications.getContent().stream()
                        .map(jobMapper::toApplicationResponse)
                        .toList());
    }

    @Override
    public ApplicationResponse updateApplicationStatus(UUID applicationId, ApplicationStatus newStatus,
                                                       String reviewNotes, UUID reviewerId) {
        log.info("Updating application {} status to {} by reviewer {}", applicationId, newStatus, reviewerId);

        JobApplication application = applicationRepository.findByApplicationIdAndIsDeletedFalse(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "applicationId", applicationId));

        // Authorization: Only job poster can review
        if (!application.getJob().getPostedByUserId().equals(reviewerId)) {
            throw new UnauthorizedAccessException("review", "this application");
        }

        // Update status
        application.setStatus(newStatus);
        application.setReviewedByUserId(reviewerId);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewNotes(reviewNotes);

        JobApplication updated = applicationRepository.save(application);

        // Send notification
        sendStatusUpdateNotification(application, newStatus, reviewNotes);

        return jobMapper.toApplicationResponse(updated);
    }

    /**
     * Send status update notification
     */
    private void sendStatusUpdateNotification(JobApplication application,
                                              ApplicationStatus newStatus,
                                              String reviewNotes) {
        try {
            notificationClient.sendApplicationStatusUpdate(
                    application.getApplicantUserId(),  // UUID
                    new NotificationServiceClient.ApplicationStatusNotification(
                            application.getApplicationId(),  // UUID
                            application.getJob().getJobTitle(),
                            newStatus.name(),
                            reviewNotes
                    )
            );
        } catch (Exception e) {
            log.error("Failed to send status update notification: {}", e.getMessage());
        }
    }

    @Override
    public void withdrawApplication(UUID applicationId, UUID userId) {
        log.info("User {} withdrawing application {}", userId, applicationId);

        JobApplication application = applicationRepository.findByApplicationIdAndIsDeletedFalse(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "applicationId", applicationId));

        // Authorization
        if (!application.getApplicantUserId().equals(userId)) {
            throw new UnauthorizedAccessException("withdraw", "this application");
        }

        // Can only withdraw if not processed
        if (application.getStatus() == ApplicationStatus.ACCEPTED ||
                application.getStatus() == ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Cannot withdraw an application that has been " +
                    application.getStatus().name().toLowerCase());
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);

        // Decrement job application count
        Job job = application.getJob();
        if (job.getApplicationCount() > 0) {
            job.setApplicationCount(job.getApplicationCount() - 1);
            jobRepository.save(job);
        }

        log.info("Application {} withdrawn successfully", applicationId);
    }
}