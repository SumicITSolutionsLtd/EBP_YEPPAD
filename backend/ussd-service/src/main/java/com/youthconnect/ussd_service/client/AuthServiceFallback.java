package com.youthconnect.ussd_service.client;

import com.youthconnect.ussd_service.dto.UssdRegistrationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Circuit Breaker Fallback for Auth Service Client
 *
 * Provides graceful degradation when auth-service is unavailable or experiencing issues.
 *
 * Fallback Strategy:
 * 1. Log the failure for monitoring/alerting
 * 2. Return user-friendly error message
 * 3. Include error code for client-side handling
 * 4. Maintain USSD-friendly message format (short, clear)
 *
 * Why Fallback is Critical:
 * - USSD users have limited screen space
 * - Need immediate feedback (no retry mechanism in USSD)
 * - Service degradation should be transparent
 *
 * Monitoring:
 * - All fallback invocations are logged with ERROR level
 * - Metrics should track fallback frequency
 * - Alerts triggered when fallback rate exceeds threshold
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class AuthServiceFallback implements AuthServiceClient {

    private static final String SERVICE_UNAVAILABLE_MESSAGE =
            "Service temporarily unavailable. Please try again in a few minutes.";

    private static final String ERROR_CODE_SERVICE_UNAVAILABLE = "AUTH_SERVICE_DOWN";

    /**
     * Fallback for user registration when auth-service is unavailable
     *
     * Impact:
     * - User cannot complete registration
     * - USSD session data is temporarily stored
     * - User receives retry instruction
     *
     * Recovery:
     * - User can retry registration after service recovery
     * - Session data persists for 24 hours
     *
     * @param request Registration request
     * @return SERVICE_UNAVAILABLE response with user guidance
     */
    @Override
    public ResponseEntity<Map<String, Object>> registerUssdUser(UssdRegistrationRequest request) {
        log.error("AUTH SERVICE FALLBACK: Registration failed for phone: {} - Service unavailable",
                maskPhone(request.getPhoneNumber()));

        // Log for analytics - track registration attempts during outage
        logFallbackEvent("REGISTRATION", request.getPhoneNumber());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", SERVICE_UNAVAILABLE_MESSAGE,
                        "errorCode", ERROR_CODE_SERVICE_UNAVAILABLE,
                        "retryable", true,
                        "estimatedRecovery", "5 minutes"
                ));
    }

    /**
     * Fallback for USSD login when auth-service is unavailable
     *
     * Impact:
     * - Existing users cannot access their accounts
     * - User receives clear service status message
     *
     * Recovery:
     * - Auto-retry mechanism can be implemented in USSD controller
     * - User notified when service recovers
     *
     * @param request Login request with phone number
     * @return SERVICE_UNAVAILABLE response
     */
    @Override
    public ResponseEntity<Map<String, Object>> loginUssdUser(Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        log.error("AUTH SERVICE FALLBACK: Login failed for phone: {} - Service unavailable",
                maskPhone(phoneNumber));

        logFallbackEvent("LOGIN", phoneNumber);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", SERVICE_UNAVAILABLE_MESSAGE,
                        "errorCode", ERROR_CODE_SERVICE_UNAVAILABLE,
                        "retryable", true
                ));
    }

    /**
     * Fallback for phone number validation when auth-service is unavailable
     *
     * Strategy:
     * - Assume phone is NOT registered (safer assumption)
     * - Allow user to proceed with registration flow
     * - Duplicate check will happen when service recovers
     *
     * @param phoneNumber Phone number to validate
     * @return Response assuming phone is not registered
     */
    @Override
    public ResponseEntity<Map<String, Object>> checkPhoneRegistered(String phoneNumber) {
        log.error("AUTH SERVICE FALLBACK: Phone check failed for: {} - Service unavailable",
                maskPhone(phoneNumber));

        logFallbackEvent("PHONE_CHECK", phoneNumber);

        // Return "not registered" to allow proceeding with registration
        // Duplicate check will happen when service recovers
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "registered", false, // Conservative assumption
                        "message", "Unable to verify. You may proceed with registration.",
                        "errorCode", ERROR_CODE_SERVICE_UNAVAILABLE
                ));
    }

    /**
     * Fallback for password reset SMS when auth-service is unavailable
     *
     * @param request Reset request
     * @return SERVICE_UNAVAILABLE response
     */
    @Override
    public ResponseEntity<Map<String, Object>> sendPasswordResetSms(Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        log.error("AUTH SERVICE FALLBACK: Password reset failed for: {} - Service unavailable",
                maskPhone(phoneNumber));

        logFallbackEvent("PASSWORD_RESET", phoneNumber);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", SERVICE_UNAVAILABLE_MESSAGE,
                        "errorCode", ERROR_CODE_SERVICE_UNAVAILABLE
                ));
    }

    /**
     * Fallback for session validation when auth-service is unavailable
     *
     * @param token Session token to validate
     * @return Invalid session response (fail-safe)
     */
    @Override
    public ResponseEntity<Map<String, Object>> validateUssdSession(String token) {
        log.error("AUTH SERVICE FALLBACK: Session validation failed - Service unavailable");

        logFallbackEvent("SESSION_VALIDATION", "N/A");

        // Fail-safe: Invalidate session when auth-service is down
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "valid", false,
                        "message", SERVICE_UNAVAILABLE_MESSAGE,
                        "errorCode", ERROR_CODE_SERVICE_UNAVAILABLE
                ));
    }

    /**
     * Mask phone number for privacy in logs
     *
     * Formats:
     * - Input: +256700123456 → Output: +256****456
     * - Input: 0700123456 → Output: 070****456
     *
     * @param phone Phone number to mask
     * @return Masked phone number
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) {
            return "****";
        }

        String cleaned = phone.replaceAll("[^0-9+]", "");

        if (cleaned.length() >= 6) {
            String prefix = cleaned.substring(0, Math.min(3, cleaned.length()));
            String suffix = cleaned.substring(Math.max(0, cleaned.length() - 3));
            return prefix + "****" + suffix;
        }

        return "****";
    }

    /**
     * Log fallback event for monitoring and analytics
     *
     * This data is used for:
     * - Service health monitoring
     * - User impact analysis
     * - Capacity planning
     * - SLA tracking
     *
     * Metrics to Track:
     * - Fallback frequency per operation type
     * - Affected user count
     * - Time to recovery
     * - User retry patterns
     *
     * @param operationType Type of operation that failed
     * @param phoneNumber Affected user's phone (masked in logs)
     */
    private void logFallbackEvent(String operationType, String phoneNumber) {
        // This would typically integrate with your monitoring system
        // Examples: Prometheus metrics, CloudWatch, Datadog, etc.

        log.warn("FALLBACK EVENT: operation={}, phone={}, timestamp={}",
                operationType,
                maskPhone(phoneNumber),
                System.currentTimeMillis());

        // TODO: Integrate with monitoring system
        // metricsService.incrementFallbackCounter(operationType);
        // alertingService.checkFallbackThreshold(operationType);
    }
}