package com.youthconnect.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * JOB NOTIFICATION DTO - UNIFIED JOB-RELATED NOTIFICATION DATA
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Unified data transfer object for all job-related notifications including:
 * - Job posting notifications (published, expiring soon)
 * - Application confirmations and status updates
 * - Job alert notifications and recommendations
 * - Application milestones for job posters
 * - Interview scheduling notifications
 *
 * Used by job-service to communicate with notification-service via Kafka/REST
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 * @since 2025-10-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobNotificationDto {

    // =========================================================================
    // NOTIFICATION METADATA
    // =========================================================================

    /**
     * Type of notification being sent
     */
    private NotificationType type;

    /**
     * Preferred delivery channel(s)
     */
    private NotificationChannel channel;

    /**
     * Notification priority (1=HIGH, 2=MEDIUM, 3=LOW)
     */
    @Builder.Default
    private Integer priority = 2;

    /**
     * Additional metadata as JSON string (for extensibility)
     */
    private String metadata;

    // =========================================================================
    // USER INFORMATION
    // =========================================================================

    /**
     * Recipient user ID (required)
     */
    private Long userId;

    /**
     * Recipient's first name (for personalization)
     */
    private String firstName;

    /**
     * Recipient's email address
     */
    private String email;

    /**
     * Recipient's phone number (for SMS notifications)
     */
    private String phoneNumber;

    /**
     * User's preferred language (en, lg, lur, lgb)
     */
    @Builder.Default
    private String preferredLanguage = "en";

    // =========================================================================
    // JOB INFORMATION
    // =========================================================================

    /**
     * Job ID (required for job-related notifications)
     */
    private Long jobId;

    /**
     * Job title
     */
    private String jobTitle;

    /**
     * Company/Organization name
     */
    private String companyName;

    /**
     * Job type (FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, VOLUNTEER)
     */
    private String jobType;

    /**
     * Work mode (REMOTE, ONSITE, HYBRID)
     */
    private String workMode;

    /**
     * Job location/district
     */
    private String location;

    /**
     * Job publication date
     */
    private LocalDateTime publishedDate;

    /**
     * Job application deadline
     */
    private LocalDateTime deadline;

    /**
     * Job expiration date
     */
    private LocalDateTime expiresDate;

    /**
     * Days remaining until deadline/expiration
     */
    private Integer daysRemaining;

    // =========================================================================
    // APPLICATION INFORMATION
    // =========================================================================

    /**
     * Application ID (if applicable)
     */
    private Long applicationId;

    /**
     * Application status (PENDING, UNDER_REVIEW, SHORTLISTED, REJECTED, ACCEPTED)
     */
    private String applicationStatus;

    /**
     * Application submission date
     */
    private LocalDateTime submittedAt;

    /**
     * Reviewer's name (for status updates)
     */
    private String reviewerName;

    /**
     * Review notes/feedback from reviewer
     */
    private String reviewNotes;

    /**
     * Next steps or additional information
     */
    private String nextSteps;

    /**
     * Interview date/time (if scheduled)
     */
    private LocalDateTime interviewDateTime;

    /**
     * Interview location or meeting link
     */
    private String interviewLocation;

    // =========================================================================
    // MILESTONE INFORMATION (FOR JOB POSTERS)
    // =========================================================================

    /**
     * Total number of applications received (for milestone notifications)
     */
    private Integer totalApplications;

    /**
     * Milestone reached (e.g., 10, 50, 100)
     */
    private Integer milestoneCount;

    // =========================================================================
    // LINKS & URLS
    // =========================================================================

    /**
     * Direct link to the job posting
     */
    private String jobUrl;

    /**
     * Direct link to the application
     */
    private String applicationUrl;

    /**
     * Link to applicant dashboard/portal
     */
    private String dashboardUrl;

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Get full display name for personalization
     */
    public String getFullName() {
        return firstName != null && !firstName.isEmpty() ? firstName : "User";
    }

    /**
     * Check if notification is urgent (deadline within 3 days)
     */
    public boolean isUrgent() {
        return daysRemaining != null && daysRemaining <= 3;
    }

    /**
     * Get notification urgency level based on days remaining
     */
    public String getUrgencyLevel() {
        if (daysRemaining == null) return "NORMAL";
        if (daysRemaining <= 1) return "CRITICAL";
        if (daysRemaining <= 3) return "HIGH";
        if (daysRemaining <= 7) return "MEDIUM";
        return "NORMAL";
    }

    /**
     * Check if this is an application-related notification
     */
    public boolean isApplicationNotification() {
        return applicationId != null && type != null &&
                type.name().startsWith("APPLICATION_");
    }

    /**
     * Check if this is a job poster notification
     */
    public boolean isJobPosterNotification() {
        return type == NotificationType.JOB_PUBLISHED ||
                type == NotificationType.JOB_EXPIRING_SOON ||
                type == NotificationType.NEW_APPLICATION_RECEIVED ||
                type == NotificationType.APPLICATION_MILESTONE;
    }

    // =========================================================================
    // ENUMS
    // =========================================================================

    /**
     * Types of job-related notifications
     */
    public enum NotificationType {
        // Job poster notifications
        JOB_PUBLISHED,
        JOB_EXPIRING_SOON,
        NEW_APPLICATION_RECEIVED,
        APPLICATION_MILESTONE,  // e.g., 10, 50, 100 applications

        // Job applicant notifications
        APPLICATION_CONFIRMED,
        APPLICATION_UNDER_REVIEW,
        APPLICATION_SHORTLISTED,
        APPLICATION_REJECTED,
        APPLICATION_ACCEPTED,
        INTERVIEW_SCHEDULED,

        // Job seeker notifications
        JOB_ALERT_NEW_MATCH,
        JOB_RECOMMENDATION,
        JOB_DEADLINE_REMINDER,
        JOB_EXPIRATION_WARNING
    }

    /**
     * Notification delivery channels
     */
    public enum NotificationChannel {
        EMAIL,      // Email only
        SMS,        // SMS only
        PUSH,       // Push notification only
        IN_APP,     // In-app notification only
        ALL         // Send via all available channels
    }
}