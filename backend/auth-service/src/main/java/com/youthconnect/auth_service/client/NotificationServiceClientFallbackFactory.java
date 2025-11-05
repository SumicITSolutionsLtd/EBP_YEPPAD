package com.youthconnect.auth_service.client;

import com.youthconnect.auth_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback Factory for Notification Service Client
 *
 * Provides graceful degradation when notification-service is unavailable.
 * Ensures that authentication operations (login, registration) continue
 * even if notifications cannot be sent.
 *
 * Pattern: Circuit Breaker with Fallback
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Component
public class NotificationServiceClientFallbackFactory implements FallbackFactory<NotificationServiceClient> {

    @Override
    public NotificationServiceClient create(Throwable cause) {
        return new NotificationServiceClient() {

            @Override
            public ApiResponse<Void> sendWelcomeEmail(String email, String role) {
                log.warn("Notification service unavailable. Failed to send welcome email to: {}. Reason: {}",
                        maskEmail(email), cause.getMessage());

                // Return success to prevent blocking registration
                return ApiResponse.success(null, "Welcome email queued (notification service unavailable)");
            }

            @Override
            public ApiResponse<Void> sendPasswordResetEmail(String email, String resetToken) {
                log.warn("Notification service unavailable. Failed to send password reset to: {}. Reason: {}",
                        maskEmail(email), cause.getMessage());

                // Return error since password reset depends on email delivery
                return ApiResponse.error("Unable to send password reset email. Please try again later.");
            }

            @Override
            public ApiResponse<Void> sendSms(String phoneNumber, String message) {
                log.warn("Notification service unavailable. Failed to send SMS to: {}. Reason: {}",
                        maskPhone(phoneNumber), cause.getMessage());

                // Return success to prevent blocking operations
                return ApiResponse.success(null, "SMS queued (notification service unavailable)");
            }

            /**
             * Mask email for privacy in logs
             */
            private String maskEmail(String email) {
                if (email == null || !email.contains("@")) {
                    return "***";
                }
                String[] parts = email.split("@");
                if (parts[0].length() <= 3) {
                    return "***@" + parts[1];
                }
                return parts[0].substring(0, 3) + "***@" + parts[1];
            }

            /**
             * Mask phone number for privacy in logs
             */
            private String maskPhone(String phone) {
                if (phone == null || phone.length() < 6) {
                    return "***";
                }
                String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");
                if (cleaned.length() >= 6) {
                    return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 3);
                }
                return "***";
            }
        };
    }
}