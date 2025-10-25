package com.youthconnect.auth_service.client;

import com.youthconnect.auth_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client for Notification Service Communication
 *
 * Handles inter-service communication with the notification-service for:
 * <ul>
 *     <li>Sending welcome emails upon user registration</li>
 *     <li>Dispatching password reset notifications</li>
 *     <li>Delivering SMS alerts via Africa's Talking API</li>
 * </ul>
 *
 * Features:
 * <ul>
 *     <li>Service discovery via Eureka</li>
 *     <li>Non-blocking calls (failures do not disrupt main flow)</li>
 *     <li>Support for email and SMS-based notifications</li>
 * </ul>
 *
 * Base Path: {@code /api/notifications}
 *
 * @author
 *     Youth Connect Uganda Development Team
 * @version
 *     1.1.0
 */
@FeignClient(
        name = "notification-service",
        path = "/api/notifications",
        fallbackFactory = NotificationServiceClientFallbackFactory.class
)
public interface NotificationServiceClient {

    /**
     * Send a welcome email to a newly registered user.
     * <p>
     * This is a non-blocking operation; registration continues even if
     * the email delivery fails.
     *
     * @param email User’s email address
     * @param role  User’s role (for personalized messaging)
     * @return ApiResponse with delivery status
     */
    @PostMapping("/welcome-email")
    ApiResponse<Void> sendWelcomeEmail(
            @RequestParam("email") String email,
            @RequestParam("role") String role
    );

    /**
     * Send a password reset email containing a secure reset token.
     *
     * @param email      User’s email address
     * @param resetToken Password reset token
     * @return ApiResponse with delivery status
     */
    @PostMapping("/password-reset")
    ApiResponse<Void> sendPasswordResetEmail(
            @RequestParam("email") String email,
            @RequestParam("token") String resetToken
    );

    /**
     * Send an SMS notification using the integrated SMS gateway.
     * <p>
     * Commonly used for OTP delivery, login alerts, or system notifications.
     *
     * @param phoneNumber Recipient’s phone number (international format)
     * @param message     SMS content
     * @return ApiResponse with delivery status
     */
    @PostMapping("/sms")
    ApiResponse<Void> sendSms(
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("message") String message
    );
}
