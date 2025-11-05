package com.youthconnect.job_services.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Notification Service Client - FIXED with UUID
 *
 * ⚠️ CHANGES in Version 3.0:
 * - All Long IDs changed to UUID
 * - All user IDs changed to UUID
 * - All job IDs changed to UUID
 * - All application IDs changed to UUID
 *
 * Sends notifications (email, SMS, push) to users for job-related events.
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (UUID Migration)
 * @since 1.0.0
 */
@FeignClient(name = "notification-service", path = "/api/v1/notifications")
public interface NotificationServiceClient {

    /**
     * Send job application confirmation to applicant
     *
     * ⚠️ CHANGED: userId parameter is now UUID (was Long)
     */
    @PostMapping("/job-application-confirmation")
    void sendApplicationConfirmation(
            @RequestParam UUID userId,  // ✅ FIXED: Long → UUID
            @RequestBody JobApplicationNotification notification
    );

    /**
     * Send new application alert to job poster
     *
     * ⚠️ CHANGED: jobPosterId parameter is now UUID (was Long)
     */
    @PostMapping("/new-job-application")
    void sendNewApplicationAlert(
            @RequestParam UUID jobPosterId,  // ✅ FIXED: Long → UUID
            @RequestBody JobApplicationNotification notification
    );

    /**
     * Send application status update (approved/rejected)
     *
     * ⚠️ CHANGED: applicantId parameter is now UUID (was Long)
     */
    @PostMapping("/application-status-update")
    void sendApplicationStatusUpdate(
            @RequestParam UUID applicantId,  // ✅ FIXED: Long → UUID
            @RequestBody ApplicationStatusNotification notification
    );

    /**
     * Send job expiration reminder
     *
     * ⚠️ CHANGED: jobPosterId parameter is now UUID (was Long)
     */
    @PostMapping("/job-expiration-reminder")
    void sendExpirationReminder(
            @RequestParam UUID jobPosterId,  // ✅ FIXED: Long → UUID
            @RequestBody JobExpirationNotification notification
    );

    /**
     * Send job alert to subscribed users
     */
    @PostMapping("/job-alert")
    void sendJobAlert(
            @RequestBody JobAlertNotification notification
    );

    /**
     * Notification DTOs - UPDATED with UUID
     */

    /**
     * Job Application Notification DTO
     *
     * ⚠️ CHANGED: jobId is now UUID (was Long)
     */
    record JobApplicationNotification(
            UUID jobId,  // ✅ FIXED: Long → UUID
            String jobTitle,
            String companyName,
            String applicantName,
            String applicantEmail
    ) {}

    /**
     * Application Status Notification DTO
     *
     * ⚠️ CHANGED: applicationId is now UUID (was Long)
     */
    record ApplicationStatusNotification(
            UUID applicationId,  // ✅ FIXED: Long → UUID
            String jobTitle,
            String status,
            String reviewNotes
    ) {}

    /**
     * Job Expiration Notification DTO
     *
     * ⚠️ CHANGED: jobId is now UUID (was Long)
     */
    record JobExpirationNotification(
            UUID jobId,  // ✅ FIXED: Long → UUID
            String jobTitle,
            int daysUntilExpiry
    ) {}

    /**
     * Job Alert Notification DTO
     *
     * ⚠️ CHANGED: userIds and jobId are now UUID (was Long)
     */
    record JobAlertNotification(
            List<UUID> userIds,  // ✅ FIXED: List<Long> → List<UUID>
            UUID jobId,          // ✅ FIXED: Long → UUID
            String jobTitle,
            String companyName,
            String location
    ) {}
}