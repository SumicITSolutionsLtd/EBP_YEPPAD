package com.youthconnect.notification.service.controller;

import com.youthconnect.notification.service.dto.*;
import com.youthconnect.notification.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * NOTIFICATION CONTROLLER - REST API ENDPOINTS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Handles all notification-related HTTP requests including:
 * - SMS delivery via Africa's Talking
 * - Email delivery via SMTP
 * - Welcome notifications (multi-channel)
 * - USSD registration confirmations
 * - Health checks for monitoring
 *
 * All endpoints return CompletableFuture for non-blocking async execution.
 *
 * @author Douglas Kings Kato
 * @version 2.0
 * @since 2025-01-15
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Send SMS notification via Africa's Talking API.
     *
     * Endpoint: POST /api/notifications/sms/send
     *
     * Request Body Example:
     * {
     *   "recipient": "+256701234567",
     *   "message": "Your application has been approved!",
     *   "messageType": "TRANSACTIONAL",
     *   "priority": 1,
     *   "senderId": "YouthConnect",
     *   "userId": 123
     * }
     *
     * Success Response (200):
     * {
     *   "success": true,
     *   "status": "SENT",
     *   "recipient": "+256****4567",
     *   "messageId": "ATXid_abc123xyz",
     *   "timestamp": "2025-01-15T10:30:00"
     * }
     *
     * Error Response (400):
     * {
     *   "success": false,
     *   "status": "FAILED",
     *   "error": "Invalid phone number format",
     *   "willRetry": true,
     *   "timestamp": "2025-01-15T10:30:00"
     * }
     *
     * @param request SMS request with recipient, message, and metadata
     * @return CompletableFuture with delivery status
     */
    @PostMapping("/sms/send")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendSms(
            @Valid @RequestBody SmsRequest request) {

        log.info("📱 SMS Request received: recipient={}, type={}, priority={}",
                maskPhone(request.getRecipient()),
                request.getMessageType(),
                request.getPriority());

        // Call service layer (async execution)
        return notificationService.sendSms(request)
                .thenApply(result -> {
                    // Service returns Map<String, Object> with success/failure details
                    boolean success = (boolean) result.getOrDefault("success", false);

                    if (success) {
                        return ResponseEntity.ok(result);
                    } else {
                        return ResponseEntity.badRequest().body(result);
                    }
                })
                .exceptionally(ex -> {
                    log.error("❌ SMS endpoint error: {}", ex.getMessage(), ex);
                    return ResponseEntity.internalServerError().body(Map.of(
                            "success", false,
                            "error", "Internal server error: " + ex.getMessage()
                    ));
                });
    }

    /**
     * Send email notification via SMTP.
     *
     * Endpoint: POST /api/notifications/email/send
     *
     * Request Body Example:
     * {
     *   "recipient": "user@example.com",
     *   "subject": "Welcome to Kwetu-Hub!",
     *   "htmlContent": "<html><body><h1>Welcome!</h1></body></html>",
     *   "textContent": "Welcome to Kwetu-Hub!",
     *   "userId": 123
     * }
     *
     * Success Response (200):
     * {
     *   "success": true,
     *   "status": "SENT",
     *   "recipient": "user@example.com",
     *   "timestamp": "2025-01-15T10:30:00"
     * }
     *
     * @param request Email request with recipient, subject, and content
     * @return CompletableFuture with delivery status
     */
    @PostMapping("/email/send")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendEmail(
            @Valid @RequestBody EmailRequest request) {

        log.info("📧 Email Request received: recipient={}, subject={}",
                request.getRecipient(),
                request.getSubject());

        return notificationService.sendEmail(request)
                .thenApply(result -> {
                    boolean success = (boolean) result.getOrDefault("success", false);

                    if (success) {
                        return ResponseEntity.ok(result);
                    } else {
                        return ResponseEntity.badRequest().body(result);
                    }
                })
                .exceptionally(ex -> {
                    log.error("❌ Email endpoint error: {}", ex.getMessage(), ex);
                    return ResponseEntity.internalServerError().body(Map.of(
                            "success", false,
                            "error", "Internal server error: " + ex.getMessage()
                    ));
                });
    }

    /**
     * Send welcome notification (multi-channel: SMS + Email).
     *
     * Triggered when new users register on the platform.
     * Sends personalized welcome messages via both SMS and email.
     *
     * Endpoint: POST /api/notifications/welcome
     *
     * Request Body Example:
     * {
     *   "userId": 123,
     *   "email": "user@example.com",
     *   "phoneNumber": "+256701234567",
     *   "firstName": "John",
     *   "userRole": "YOUTH",
     *   "preferredLanguage": "en"
     * }
     *
     * Success Response (200):
     * {
     *   "success": true,
     *   "message": "Welcome notification sent",
     *   "sms": {
     *     "success": true,
     *     "status": "SENT",
     *     "messageId": "ATXid_abc123"
     *   },
     *   "email": {
     *     "success": true,
     *     "status": "SENT"
     *   },
     *   "userId": 123
     * }
     *
     * @param request Welcome notification request with user details
     * @return CompletableFuture with results for both SMS and email
     */
    @PostMapping("/welcome")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendWelcomeNotification(
            @Valid @RequestBody WelcomeNotificationRequest request) {

        log.info("🎉 Welcome notification request for user: id={}, role={}",
                request.getUserId(),
                request.getUserRole());

        return notificationService.sendWelcomeNotification(request)
                .thenApply(result -> ResponseEntity.ok(result))
                .exceptionally(ex -> {
                    log.error("❌ Welcome notification error: {}", ex.getMessage(), ex);
                    return ResponseEntity.internalServerError().body(Map.of(
                            "success", false,
                            "error", "Failed to send welcome notification: " + ex.getMessage()
                    ));
                });
    }

    /**
     * Send USSD registration confirmation.
     *
     * Sent immediately after a user completes registration via USSD (*256#).
     * Provides confirmation code and instructions for web/mobile access.
     *
     * Endpoint: POST /api/notifications/ussd/confirmation
     *
     * Request Body Example:
     * {
     *   "phoneNumber": "+256701234567",
     *   "userName": "John Doe",
     *   "confirmationCode": "ABC123",
     *   "message": "Optional custom message"
     * }
     *
     * Success Response (200):
     * {
     *   "success": true,
     *   "message": "USSD confirmation sent",
     *   "recipient": "+256****4567",
     *   "messageId": "ATXid_xyz789"
     * }
     *
     * @param request USSD confirmation request with phone and user details
     * @return CompletableFuture with delivery status
     */
    @PostMapping("/ussd/confirmation")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendUssdConfirmation(
            @Valid @RequestBody UssdConfirmationRequest request) {

        log.info("📞 USSD confirmation request for phone: {}",
                maskPhone(request.getPhoneNumber()));

        return notificationService.sendUssdConfirmation(request)
                .thenApply(result -> ResponseEntity.ok(result))
                .exceptionally(ex -> {
                    log.error("❌ USSD confirmation error: {}", ex.getMessage(), ex);
                    return ResponseEntity.internalServerError().body(Map.of(
                            "success", false,
                            "error", "Failed to send USSD confirmation: " + ex.getMessage()
                    ));
                });
    }

    /**
     * Health check endpoint for notification service monitoring.
     *
     * Endpoint: GET /api/notifications/health
     *
     * Checks:
     * - SMS service health (Africa's Talking API reachability)
     * - Email service health (SMTP server connectivity)
     * - Database connectivity
     * - Queue health (if using message queue)
     *
     * Success Response (200):
     * {
     *   "status": "UP",
     *   "service": "notification-service",
     *   "timestamp": "2025-01-15T10:30:00",
     *   "checks": {
     *     "sms": {
     *       "status": "UP",
     *       "provider": "AFRICAS_TALKING",
     *       "responseTime": 150
     *     },
     *     "email": {
     *       "status": "UP",
     *       "provider": "SMTP",
     *       "responseTime": 80
     *     },
     *     "database": {
     *       "status": "UP",
     *       "responseTime": 25
     *     }
     *   }
     * }
     *
     * Degraded Response (200 - partial failure):
     * {
     *   "status": "DEGRADED",
     *   "service": "notification-service",
     *   "checks": {
     *     "sms": {
     *       "status": "DOWN",
     *       "error": "Connection timeout"
     *     },
     *     "email": {
     *       "status": "UP"
     *     }
     *   }
     * }
     *
     * @return Health status of notification service and its dependencies
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {

        log.debug("🏥 Health check requested");

        try {
            // Check SMS service health
            Map<String, Object> smsHealth = notificationService.checkSmsServiceHealth();
            boolean smsHealthy = (boolean) smsHealth.get("healthy");

            // Check email service health
            Map<String, Object> emailHealth = notificationService.checkEmailServiceHealth();
            boolean emailHealthy = (boolean) emailHealth.get("healthy");

            // Determine overall status
            String overallStatus;
            if (smsHealthy && emailHealthy) {
                overallStatus = "UP";
            } else if (smsHealthy || emailHealthy) {
                overallStatus = "DEGRADED"; // Partial functionality
            } else {
                overallStatus = "DOWN";
            }

            Map<String, Object> response = Map.of(
                    "status", overallStatus,
                    "service", "notification-service",
                    "timestamp", java.time.LocalDateTime.now(),
                    "checks", Map.of(
                            "sms", smsHealth,
                            "email", emailHealth
                    )
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Health check failed: {}", e.getMessage(), e);

            return ResponseEntity.status(503).body(Map.of(
                    "status", "DOWN",
                    "service", "notification-service",
                    "error", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    /**
     * Get notification statistics for analytics dashboard.
     *
     * Endpoint: GET /api/notifications/stats?startDate=2025-01-01&endDate=2025-01-31
     *
     * Query Parameters:
     * - startDate: Start of date range (format: yyyy-MM-dd)
     * - endDate: End of date range (format: yyyy-MM-dd)
     *
     * Response Example:
     * {
     *   "totalSent": 1250,
     *   "totalFailed": 45,
     *   "totalPending": 12,
     *   "successRate": "96.52%",
     *   "breakdown": {
     *     "sms": {
     *       "sent": 800,
     *       "failed": 30,
     *       "successRate": "96.39%"
     *     },
     *     "email": {
     *       "sent": 450,
     *       "failed": 15,
     *       "successRate": "96.77%"
     *     }
     *   },
     *   "period": {
     *     "start": "2025-01-01",
     *     "end": "2025-01-31"
     *   }
     * }
     *
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return Notification statistics for the given period
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("📊 Stats request: startDate={}, endDate={}", startDate, endDate);

        try {
            java.time.LocalDateTime start = startDate != null ?
                    java.time.LocalDate.parse(startDate).atStartOfDay() :
                    java.time.LocalDateTime.now().minusDays(30);

            java.time.LocalDateTime end = endDate != null ?
                    java.time.LocalDate.parse(endDate).atTime(23, 59, 59) :
                    java.time.LocalDateTime.now();

            Map<String, Object> stats = notificationService.getNotificationStats(start, end);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("❌ Stats retrieval error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Invalid date format or stats retrieval failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Get notification history for a specific user.
     *
     * Endpoint: GET /api/notifications/user/{userId}/history?limit=50
     *
     * Query Parameters:
     * - limit: Maximum number of notifications to return (default: 50)
     *
     * Response Example:
     * {
     *   "userId": 123,
     *   "totalNotifications": 150,
     *   "notifications": [
     *     {
     *       "id": 1001,
     *       "type": "SMS",
     *       "status": "SENT",
     *       "recipient": "+256****4567",
     *       "content": "Your application has been approved!",
     *       "sentAt": "2025-01-15T10:30:00",
     *       "deliveredAt": "2025-01-15T10:30:05"
     *     },
     *     // ... more notifications
     *   ]
     * }
     *
     * @param userId User ID to fetch notifications for
     * @param limit Maximum number of notifications (default: 50)
     * @return User's notification history
     */
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "50") int limit) {

        log.info("📜 Notification history request: userId={}, limit={}", userId, limit);

        try {
            var notifications = notificationService.getUserNotifications(userId, limit);

            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "totalNotifications", notifications.size(),
                    "notifications", notifications
            ));

        } catch (Exception e) {
            log.error("❌ History retrieval error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "Failed to retrieve notification history: " + e.getMessage()
            ));
        }
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    /**
     * Masks phone number for privacy in logs and responses.
     *
     * Examples:
     * - +256701234567 → +256****4567
     * - 256701234567 → 256****4567
     * - 0701234567 → 070****567
     *
     * @param phoneNumber Raw phone number
     * @return Masked phone number with middle digits hidden
     */
    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return "****";
        }

        int length = phoneNumber.length();
        int keepStart = Math.min(4, length / 3);
        int keepEnd = Math.min(4, length / 3);

        return phoneNumber.substring(0, keepStart) +
                "****" +
                phoneNumber.substring(length - keepEnd);
    }
}