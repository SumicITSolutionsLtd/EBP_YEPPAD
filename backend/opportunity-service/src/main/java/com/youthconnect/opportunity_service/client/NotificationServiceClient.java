package com.youthconnect.opportunity_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for sending notifications via Notification Service
 * Supports SMS, Email, and Push notifications
 */
@FeignClient(name = "notification-service", path = "/api/notifications")
public interface NotificationServiceClient {

    /**
     * Notify user that their application was submitted successfully
     */
    @PostMapping("/application-submitted")
    void sendApplicationSubmittedNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("opportunityTitle") String opportunityTitle,
            @RequestParam("applicationId") Long applicationId
    );

    /**
     * Notify user that their application is under review
     */
    @PostMapping("/application-under-review")
    void sendApplicationUnderReviewNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("applicationId") Long applicationId
    );

    /**
     * Notify user that their application was approved
     */
    @PostMapping("/application-approved")
    void sendApplicationApprovedNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("applicationId") Long applicationId
    );

    /**
     * Notify user that their application was rejected
     */
    @PostMapping("/application-rejected")
    void sendApplicationRejectedNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("reason") String reason
    );

    /**
     * Notify opportunity poster about new application
     */
    @PostMapping("/new-application-received")
    void sendNewApplicationNotification(
            @RequestParam("posterId") Long posterId,
            @RequestParam("opportunityId") Long opportunityId,
            @RequestParam("applicantName") String applicantName
    );

    /**
     * Send deadline reminder to users
     */
    @PostMapping("/opportunity-deadline-reminder")
    void sendDeadlineReminder(
            @RequestParam("userId") Long userId,
            @RequestParam("opportunityTitle") String opportunityTitle,
            @RequestParam("daysRemaining") int daysRemaining
    );

    /**
     * Notify users about new matching opportunities
     */
    @PostMapping("/new-opportunity-match")
    void sendOpportunityMatchNotification(
            @RequestParam("userId") Long userId,
            @RequestParam("opportunityId") Long opportunityId,
            @RequestParam("matchScore") double matchScore
    );
}