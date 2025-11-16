package com.youthconnect.notification.service.controller;

import com.youthconnect.notification.service.dto.*;
import com.youthconnect.notification.service.entity.NotificationLog;
import com.youthconnect.notification.service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * NOTIFICATION CONTROLLER - REST API ENDPOINTS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * ✅ COMPLIANCE CHECKLIST:
 * ✅ Returns DTOs only (no ResponseEntity)
 * ✅ All list endpoints use pagination (Page<T>)
 * ✅ UUID-based IDs throughout
 * ✅ Public health check endpoint (no auth required)
 * ✅ All other endpoints rely on API Gateway for auth
 *
 * Authentication Flow:
 * - API Gateway validates JWT tokens
 * - Gateway adds user context headers (X-User-Id, X-User-Role)
 * - This service remains stateless
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (Guidelines Compliant)
 * @since 2025-11-06
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Service", description = "Multi-channel notification delivery API")
public class NotificationController {

    private final NotificationService notificationService;

    // =========================================================================
    // PUBLIC HEALTH CHECK ENDPOINT (Required by guidelines - No Auth)
    // =========================================================================

    /**
     * Public health check endpoint
     *
     * Authentication: NONE (Public endpoint)
     * Used by: API Gateway, Kubernetes probes, monitoring systems
     *
     * @return Health status DTO (NOT Map)
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Public health check endpoint for monitoring")
    public HealthCheckResponse healthCheck() {
        log.debug("🏥 Health check requested");

        try {
            // Check SMS service health
            Map<String, Object> smsHealth = notificationService.checkSmsServiceHealth();
            boolean smsHealthy = (boolean) smsHealth.get("healthy");

            // Check email service health
            Map<String, Object> emailHealth = notificationService.checkEmailServiceHealth();
            boolean emailHealthy = (boolean) emailHealth.get("healthy");

            // Determine overall status
            String overallStatus = (smsHealthy && emailHealthy) ? "UP" :
                    (smsHealthy || emailHealthy) ? "DEGRADED" : "DOWN";

            // Build typed response DTO
            return HealthCheckResponse.builder()
                    .status(overallStatus)
                    .service("notification-service")
                    .timestamp(LocalDateTime.now())
                    .checks(Map.of(
                            "sms", HealthCheckResponse.ServiceHealth.builder()
                                    .healthy(smsHealthy)
                                    .status((String) smsHealth.get("status"))
                                    .provider((String) smsHealth.get("provider"))
                                    .responseTime(((Number) smsHealth.getOrDefault("responseTime", 0)).longValue())
                                    .error((String) smsHealth.get("error"))
                                    .build(),
                            "email", HealthCheckResponse.ServiceHealth.builder()
                                    .healthy(emailHealthy)
                                    .status((String) emailHealth.get("status"))
                                    .provider((String) emailHealth.get("provider"))
                                    .responseTime(((Number) emailHealth.getOrDefault("responseTime", 0)).longValue())
                                    .error((String) emailHealth.get("error"))
                                    .build()
                    ))
                    .build();

        } catch (Exception e) {
            log.error("❌ Health check failed: {}", e.getMessage(), e);

            return HealthCheckResponse.builder()
                    .status("DOWN")
                    .service("notification-service")
                    .timestamp(LocalDateTime.now())
                    .checks(Map.of(
                            "error", HealthCheckResponse.ServiceHealth.builder()
                                    .healthy(false)
                                    .status("ERROR")
                                    .error(e.getMessage())
                                    .build()
                    ))
                    .build();
        }
    }

    // =========================================================================
    // SMS ENDPOINTS (Protected by API Gateway)
    // =========================================================================

    /**
     * Send SMS notification via Africa's Talking API
     *
     * Authentication: Required (via API Gateway)
     * Headers: X-User-Id, X-User-Role (added by gateway)
     *
     * ✅ FIXED: Returns DTO instead of CompletableFuture<Map>
     *
     * @param request SMS request with recipient, message, and metadata
     * @return NotificationResponse DTO with delivery status
     */
    @PostMapping("/sms/send")
    @Operation(summary = "Send SMS", description = "Send SMS notification via Africa's Talking")
    public NotificationResponse sendSms(@Valid @RequestBody SmsRequest request) {

        log.info("📱 SMS Request received: recipient={}, type={}, priority={}",
                maskPhone(request.getRecipient()),
                request.getMessageType(),
                request.getPriority());

        try {
            // ✅ FIXED: Convert service result to DTO
            Map<String, Object> result = notificationService.sendSms(request).join();

            return NotificationResponse.builder()
                    .success((boolean) result.get("success"))
                    .status((String) result.get("status"))
                    .messageId((String) result.get("messageId"))
                    .recipient((String) result.get("recipient"))
                    .error((String) result.get("error"))
                    .willRetry((Boolean) result.get("willRetry"))
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("❌ SMS sending failed: {}", e.getMessage(), e);

            return NotificationResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    // =========================================================================
    // EMAIL ENDPOINTS (Protected by API Gateway)
    // =========================================================================

    /**
     * Send email notification via SMTP
     *
     * Authentication: Required (via API Gateway)
     *
     * ✅ FIXED: Returns DTO instead of CompletableFuture<Map>
     *
     * @param request Email request with recipient, subject, and content
     * @return NotificationResponse DTO with delivery status
     */
    @PostMapping("/email/send")
    @Operation(summary = "Send Email", description = "Send email notification via SMTP")
    public NotificationResponse sendEmail(@Valid @RequestBody EmailRequest request) {

        log.info("📧 Email Request received: recipient={}, subject={}",
                request.getRecipient(),
                request.getSubject());

        try {
            Map<String, Object> result = notificationService.sendEmail(request).join();

            return NotificationResponse.builder()
                    .success((boolean) result.get("success"))
                    .status((String) result.get("status"))
                    .recipient((String) result.get("recipient"))
                    .error((String) result.get("error"))
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("❌ Email sending failed: {}", e.getMessage(), e);

            return NotificationResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    // =========================================================================
    // WELCOME NOTIFICATION (MULTI-CHANNEL - Protected by API Gateway)
    // =========================================================================

    /**
     * Send welcome notification (multi-channel: SMS + Email)
     *
     * Authentication: Required (via API Gateway)
     * Triggered when new users register on the platform.
     *
     * ✅ FIXED: Returns DTO instead of CompletableFuture<Map>
     *
     * @param request Welcome notification request with user details
     * @return NotificationResponse with results for both SMS and email
     */
    @PostMapping("/welcome")
    @Operation(summary = "Send Welcome Notification",
            description = "Send multi-channel welcome notification for new users")
    public NotificationResponse sendWelcomeNotification(@Valid @RequestBody WelcomeNotificationRequest request) {

        log.info("🎉 Welcome notification request for user: id={}, role={}",
                request.getUserId(),
                request.getUserRole());

        try {
            Map<String, Object> result = notificationService.sendWelcomeNotification(request).join();

            return NotificationResponse.builder()
                    .success((boolean) result.get("success"))
                    .status("SENT")
                    .metadata(result) // Contains SMS and email results
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("❌ Welcome notification failed: {}", e.getMessage(), e);

            return NotificationResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    // =========================================================================
    // USSD REGISTRATION CONFIRMATION (Protected by API Gateway)
    // =========================================================================

    /**
     * Send USSD registration confirmation
     *
     * Authentication: Required (via API Gateway)
     * Sent immediately after USSD registration (*256#).
     *
     * ✅ FIXED: Returns DTO instead of CompletableFuture<Map>
     *
     * @param request USSD confirmation request with phone and user details
     * @return NotificationResponse with delivery status
     */
    @PostMapping("/ussd/confirmation")
    @Operation(summary = "Send USSD Confirmation",
            description = "Send USSD registration confirmation SMS")
    public NotificationResponse sendUssdConfirmation(@Valid @RequestBody UssdConfirmationRequest request) {

        log.info("📞 USSD confirmation request for phone: {}",
                maskPhone(request.getPhoneNumber()));

        try {
            Map<String, Object> result = notificationService.sendUssdConfirmation(request).join();

            return NotificationResponse.builder()
                    .success((boolean) result.get("success"))
                    .status((String) result.get("status"))
                    .messageId((String) result.get("messageId"))
                    .recipient((String) result.get("recipient"))
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("❌ USSD confirmation failed: {}", e.getMessage(), e);

            return NotificationResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .error(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    // =========================================================================
    // STATISTICS & ANALYTICS (WITH PAGINATION - Protected by API Gateway)
    // =========================================================================

    /**
     * Get notification statistics for analytics dashboard
     *
     * Authentication: Required (Admin role via API Gateway)
     *
     * ✅ FIXED: Returns DTO instead of Map
     *
     * @param startDate Start of date range (format: yyyy-MM-dd)
     * @param endDate   End of date range (format: yyyy-MM-dd)
     * @return NotificationStatsResponse DTO
     */
    @GetMapping("/stats")
    @Operation(summary = "Get Notification Statistics",
            description = "Retrieve notification delivery statistics for a date range")
    public NotificationStatsResponse getNotificationStats(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam(required = false) String endDate) {

        log.info("📊 Stats request: startDate={}, endDate={}", startDate, endDate);

        LocalDateTime start = startDate != null ?
                java.time.LocalDate.parse(startDate).atStartOfDay() :
                LocalDateTime.now().minusDays(30);

        LocalDateTime end = endDate != null ?
                java.time.LocalDate.parse(endDate).atTime(23, 59, 59) :
                LocalDateTime.now();

        Map<String, Object> stats = notificationService.getNotificationStats(start, end);

        // ✅ FIXED: Convert Map to typed DTO
        return convertStatsToDto(stats, start, end);
    }

    // =========================================================================
    // USER NOTIFICATION HISTORY (WITH PAGINATION - Protected by API Gateway)
    // =========================================================================

    /**
     * Get notification history for a specific user (PAGINATED)
     *
     * Authentication: Required (via API Gateway)
     * Note: API Gateway should verify user can only access their own history
     *
     * ✅ COMPLIANT: Returns Page<T> for pagination
     *
     * @param userId User UUID
     * @param page   Page number (default: 0)
     * @param size   Page size (default: 20)
     * @param sort   Sort field (default: createdAt)
     * @param order  Sort order (default: desc)
     * @return Paginated notification history (Page<NotificationLog>)
     */
    @GetMapping("/user/{userId}/history")
    @Operation(summary = "Get User Notification History",
            description = "Retrieve paginated notification history for a specific user")
    public Page<NotificationLog> getUserNotifications(
            @Parameter(description = "User UUID")
            @PathVariable UUID userId,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort order (asc/desc)")
            @RequestParam(defaultValue = "desc") String order) {

        log.info("📜 Notification history request: userId={}, page={}, size={}",
                userId, page, size);

        // ✅ COMPLIANT: Create pageable with sorting
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        return notificationService.getUserNotifications(userId, pageable);
    }

    // =========================================================================
    // RECENT NOTIFICATIONS (WITH PAGINATION - Protected by API Gateway)
    // =========================================================================

    /**
     * Get recent notifications for a user (last 30 days)
     *
     * Authentication: Required (via API Gateway)
     *
     * ✅ COMPLIANT: Returns Page<T> for pagination
     *
     * @param userId User UUID
     * @param page   Page number (default: 0)
     * @param size   Page size (default: 10)
     * @return Paginated recent notifications
     */
    @GetMapping("/user/{userId}/recent")
    @Operation(summary = "Get Recent Notifications",
            description = "Get recent notifications for a user (last 30 days)")
    public Page<NotificationLog> getRecentNotifications(
            @Parameter(description = "User UUID")
            @PathVariable UUID userId,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {

        log.info("🔔 Recent notifications request: userId={}, page={}, size={}",
                userId, page, size);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return notificationService.getRecentNotifications(userId, pageable);
    }

    // =========================================================================
    // FAILED NOTIFICATIONS (WITH PAGINATION - Admin Only via API Gateway)
    // =========================================================================

    /**
     * Get failed notifications pending retry (PAGINATED)
     *
     * Authentication: Required (Admin role via API Gateway)
     *
     * ✅ COMPLIANT: Returns Page<T> for pagination
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 50)
     * @return Paginated list of failed notifications pending retry
     */
    @GetMapping("/failed")
    @Operation(summary = "Get Failed Notifications",
            description = "Retrieve failed notifications pending retry (Admin only)")
    public Page<NotificationLog> getFailedNotifications(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "50") int size) {

        log.info("❌ Failed notifications request: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "nextRetryAt"));

        return notificationService.getFailedNotifications(pageable);
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    /**
     * Masks phone number for privacy in logs and responses
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

    /**
     * Convert Map statistics to typed DTO
     *
     * @param stats Raw statistics map
     * @param start Period start date
     * @param end Period end date
     * @return Typed NotificationStatsResponse DTO
     */
    @SuppressWarnings("unchecked")
    private NotificationStatsResponse convertStatsToDto(
            Map<String, Object> stats,
            LocalDateTime start,
            LocalDateTime end) {

        Map<String, Object> breakdown = (Map<String, Object>) stats.get("breakdown");
        Map<String, Object> smsStats = (Map<String, Object>) breakdown.get("sms");
        Map<String, Object> emailStats = (Map<String, Object>) breakdown.get("email");

        return NotificationStatsResponse.builder()
                .totalSent(((Number) stats.get("totalSent")).longValue())
                .totalFailed(((Number) stats.get("totalFailed")).longValue())
                .totalPending(((Number) stats.get("totalPending")).longValue())
                .successRate((String) stats.get("successRate"))
                .sms(NotificationStatsResponse.ChannelStats.builder()
                        .sent(((Number) smsStats.get("sent")).longValue())
                        .failed(((Number) smsStats.get("failed")).longValue())
                        .successRate((String) smsStats.get("successRate"))
                        .build())
                .email(NotificationStatsResponse.ChannelStats.builder()
                        .sent(((Number) emailStats.get("sent")).longValue())
                        .failed(((Number) emailStats.get("failed")).longValue())
                        .successRate((String) emailStats.get("successRate"))
                        .build())
                .periodStart(start)
                .periodEnd(end)
                .build();
    }
}