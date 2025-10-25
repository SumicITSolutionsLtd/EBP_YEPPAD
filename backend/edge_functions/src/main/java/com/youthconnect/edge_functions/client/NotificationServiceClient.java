package com.youthconnect.edge_functions.client;

import com.youthconnect.edge_functions.dto.request.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign Client for Notification Service
 *
 * Handles multi-channel notification delivery:
 * - SMS via Africa's Talking
 * - Email via SMTP/SendGrid
 * - Push notifications (Firebase)
 * - In-app notifications
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@FeignClient(
        name = "notification-service",
        path = "/api/notifications"
)
public interface NotificationServiceClient {

    /**
     * Send a notification through specified channel
     *
     * @param request Notification details (recipient, content, channel)
     * @return Response with delivery status
     */
    @PostMapping("/send")
    ResponseEntity<Map<String, Object>> sendNotification(
            @RequestBody NotificationRequest request
    );

    /**
     * Send SMS notification
     *
     * @param phoneNumber Recipient phone number (Uganda format)
     * @param message SMS content (max 160 chars for single SMS)
     * @return Delivery status and message ID
     */
    @PostMapping("/sms")
    ResponseEntity<Map<String, Object>> sendSMS(
            @RequestParam String phoneNumber,
            @RequestParam String message
    );

    /**
     * Send email notification
     *
     * @param email Recipient email address
     * @param subject Email subject line
     * @param body Email body (HTML supported)
     * @return Delivery status
     */
    @PostMapping("/email")
    ResponseEntity<Map<String, Object>> sendEmail(
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String body
    );

    /**
     * Send bulk SMS (for campaigns or alerts)
     *
     * @param phoneNumbers List of recipient phone numbers
     * @param message SMS content
     * @return Batch delivery status
     */
    @PostMapping("/sms/bulk")
    ResponseEntity<Map<String, Object>> sendBulkSMS(
            @RequestBody Map<String, Object> request
    );

    /**
     * Get notification delivery status
     *
     * @param notificationId Notification ID from send response
     * @return Delivery status (PENDING, SENT, DELIVERED, FAILED)
     */
    @GetMapping("/{notificationId}/status")
    ResponseEntity<Map<String, Object>> getNotificationStatus(
            @PathVariable String notificationId
    );

    /**
     * Get user notification preferences
     *
     * @param userId User ID
     * @return Notification preferences (channels, frequency, etc.)
     */
    @GetMapping("/preferences/{userId}")
    ResponseEntity<Map<String, Object>> getUserPreferences(
            @PathVariable Long userId
    );

    /**
     * Update user notification preferences
     *
     * @param userId User ID
     * @param preferences New preference settings
     * @return Updated preferences
     */
    @PutMapping("/preferences/{userId}")
    ResponseEntity<Map<String, Object>> updateUserPreferences(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> preferences
    );
}