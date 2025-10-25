package com.youthconnect.opportunity_service.service;

import com.youthconnect.opportunity_service.client.NotificationServiceClient;
import com.youthconnect.opportunity_service.client.UserServiceClient;
import com.youthconnect.opportunity_service.dto.ApplicationDTO;
import com.youthconnect.opportunity_service.dto.ApplicationRequest;
import com.youthconnect.opportunity_service.entity.Application;
import com.youthconnect.opportunity_service.entity.Opportunity;
import com.youthconnect.opportunity_service.exception.InvalidApplicationException;
import com.youthconnect.opportunity_service.exception.OpportunityNotFoundException;
import com.youthconnect.opportunity_service.repository.ApplicationRepository;
import com.youthconnect.opportunity_service.repository.OpportunityRepository;
import com.youthconnect.opportunity_service.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing application submissions and reviews
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final ApplicationProperties properties;

    /**
     * Submit a new application to an opportunity
     */
    @Transactional
    public ApplicationDTO submitApplication(ApplicationRequest request) {
        log.info("Processing application submission for opportunity: {}, user: {}",
                request.getOpportunityId(), request.getApplicantId());

        // 1. Validate opportunity exists and is open
        Opportunity opportunity = opportunityRepository.findById(request.getOpportunityId())
                .orElseThrow(() -> new OpportunityNotFoundException(
                        "Opportunity not found: " + request.getOpportunityId()));

        if (opportunity.getStatus() != Opportunity.Status.OPEN) {
            throw new InvalidApplicationException("Opportunity is not open for applications");
        }

        // 2. Check application deadline
        if (opportunity.getApplicationDeadline() != null &&
                LocalDateTime.now().isAfter(opportunity.getApplicationDeadline())) {
            throw new InvalidApplicationException("Application deadline has passed");
        }

        // 3. Check for duplicate application
        if (applicationRepository.existsByOpportunityIdAndApplicantId(
                request.getOpportunityId(), request.getApplicantId())) {
            throw new InvalidApplicationException("You have already applied to this opportunity");
        }

        // 4. Rate limiting: Check daily application limit
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long applicationsToday = applicationRepository.countUserApplicationsToday(
                request.getApplicantId(), startOfDay);

        if (applicationsToday >= properties.getApplication().getMaxApplicationsPerDay()) {
            throw new InvalidApplicationException(
                    "Daily application limit reached. Please try again tomorrow.");
        }

        // 5. Create and save application
        Application application = new Application();
        application.setOpportunityId(request.getOpportunityId());
        application.setApplicantId(request.getApplicantId());
        application.setApplicationContent(request.getApplicationContent());
        application.setStatus(Application.Status.PENDING);

        Application savedApplication = applicationRepository.save(application);
        log.info("Application submitted successfully. ID: {}", savedApplication.getApplicationId());

        // 6. Send notification asynchronously
        sendApplicationNotification(savedApplication, opportunity);

        return convertToDto(savedApplication);
    }

    /**
     * Get all applications for an opportunity (NGO/Funder view)
     */
    @Transactional(readOnly = true)
    public List<ApplicationDTO> getApplicationsByOpportunity(Long opportunityId) {
        log.debug("Fetching applications for opportunity: {}", opportunityId);

        List<Application> applications = applicationRepository.findByOpportunityId(opportunityId);
        return applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all applications by a user (Youth view)
     */
    @Transactional(readOnly = true)
    public List<ApplicationDTO> getUserApplications(Long userId) {
        log.debug("Fetching applications for user: {}", userId);

        List<Application> applications = applicationRepository.findByApplicantId(userId);
        return applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Review an application (Approve/Reject)
     */
    @Transactional
    public ApplicationDTO reviewApplication(Long applicationId, ApplicationRequest reviewRequest) {
        log.info("Reviewing application: {}", applicationId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        // Update review information
        application.setReviewedById(reviewRequest.getReviewedById());
        application.setReviewNotes(reviewRequest.getReviewNotes());
        application.setReviewedAt(LocalDateTime.now());
        application.setStatus(Application.Status.UNDER_REVIEW);

        Application reviewed = applicationRepository.save(application);
        log.info("Application reviewed successfully. ID: {}", applicationId);

        // Send notification to applicant
        sendReviewNotification(reviewed);

        return convertToDto(reviewed);
    }

    /**
     * Approve an application
     */
    @Transactional
    public ApplicationDTO approveApplication(Long applicationId, Long reviewerId, String notes) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setStatus(Application.Status.APPROVED);
        application.setReviewedById(reviewerId);
        application.setReviewNotes(notes);
        application.setReviewedAt(LocalDateTime.now());

        Application approved = applicationRepository.save(application);
        log.info("Application approved. ID: {}", applicationId);

        sendApprovalNotification(approved);
        return convertToDto(approved);
    }

    /**
     * Reject an application
     */
    @Transactional
    public ApplicationDTO rejectApplication(Long applicationId, Long reviewerId, String notes) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setStatus(Application.Status.REJECTED);
        application.setReviewedById(reviewerId);
        application.setReviewNotes(notes);
        application.setReviewedAt(LocalDateTime.now());

        Application rejected = applicationRepository.save(application);
        log.info("Application rejected. ID: {}", applicationId);

        sendRejectionNotification(rejected);
        return convertToDto(rejected);
    }

    /**
     * Withdraw an application (by applicant)
     */
    @Transactional
    public void withdrawApplication(Long applicationId, Long userId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Verify ownership
        if (!application.getApplicantId().equals(userId)) {
            throw new InvalidApplicationException("You can only withdraw your own applications");
        }

        // Check if withdrawable
        if (!application.isWithdrawable()) {
            throw new InvalidApplicationException("Application cannot be withdrawn at this stage");
        }

        application.setStatus(Application.Status.WITHDRAWN);
        applicationRepository.save(application);
        log.info("Application withdrawn. ID: {}", applicationId);
    }

    // ============================================================================
    // NOTIFICATION METHODS (Async)
    // ============================================================================

    @Async("notificationExecutor")
    protected void sendApplicationNotification(Application application, Opportunity opportunity) {
        try {
            log.debug("Sending application submission notification");
            // Call notification service via Feign client
            notificationServiceClient.sendApplicationSubmittedNotification(
                    application.getApplicantId(),
                    opportunity.getTitle(),
                    application.getApplicationId()
            );
        } catch (Exception e) {
            log.error("Failed to send application notification", e);
        }
    }

    @Async("notificationExecutor")
    protected void sendReviewNotification(Application application) {
        try {
            notificationServiceClient.sendApplicationUnderReviewNotification(
                    application.getApplicantId(),
                    application.getApplicationId()
            );
        } catch (Exception e) {
            log.error("Failed to send review notification", e);
        }
    }

    @Async("notificationExecutor")
    protected void sendApprovalNotification(Application application) {
        try {
            notificationServiceClient.sendApplicationApprovedNotification(
                    application.getApplicantId(),
                    application.getApplicationId()
            );
        } catch (Exception e) {
            log.error("Failed to send approval notification", e);
        }
    }

    @Async("notificationExecutor")
    protected void sendRejectionNotification(Application application) {
        try {
            notificationServiceClient.sendApplicationRejectedNotification(
                    application.getApplicantId(),
                    application.getApplicationId(),
                    application.getReviewNotes()
            );
        } catch (Exception e) {
            log.error("Failed to send rejection notification", e);
        }
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private ApplicationDTO convertToDto(Application application) {
        long daysSince = ChronoUnit.DAYS.between(
                application.getSubmittedAt(), LocalDateTime.now());

        return ApplicationDTO.builder()
                .applicationId(application.getApplicationId())
                .opportunityId(application.getOpportunityId())
                .applicantId(application.getApplicantId())
                .status(application.getStatus())
                .applicationContent(application.getApplicationContent())
                .reviewedById(application.getReviewedById())
                .reviewNotes(application.getReviewNotes())
                .reviewedAt(application.getReviewedAt())
                .submittedAt(application.getSubmittedAt())
                .updatedAt(application.getUpdatedAt())
                .isEditable(application.isEditable())
                .isWithdrawable(application.isWithdrawable())
                .daysSinceSubmission((int) daysSince)
                .build();
    }
}
