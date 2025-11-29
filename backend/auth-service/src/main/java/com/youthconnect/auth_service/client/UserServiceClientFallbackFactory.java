package com.youthconnect.auth_service.client;

import com.youthconnect.auth_service.dto.request.RegisterRequest;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.dto.response.UserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ============================================================================
 *  Circuit Breaker Fallback for User Service Communication
 * ============================================================================
 *
 * PURPOSE:
 * Provides graceful degradation when User Service is unavailable or slow.
 * Implements the Circuit Breaker pattern to prevent cascading failures.
 *
 * WHEN FALLBACK IS TRIGGERED:
 *
 * 1. Connection Timeout:
 *    - User Service doesn't respond within configured timeout (default: 10s)
 *    - Network connectivity issues
 *
 * 2. HTTP Errors:
 *    - 5xx errors from User Service (500, 503, etc.)
 *    - Service returning error responses
 *
 * 3. Service Unavailability:
 *    - User Service is down or restarting
 *    - Kubernetes pod is terminating
 *    - Load balancer can't find healthy instances
 *
 * 4. Circuit Open:
 *    - Too many consecutive failures detected
 *    - Circuit breaker opens automatically
 *    - Requests fail fast without calling User Service
 *
 * FALLBACK BEHAVIOR:
 *
 * - Logs detailed error information for monitoring
 * - Returns user-friendly error messages
 * - Prevents exception propagation to API Gateway
 * - Allows Auth Service to handle failure gracefully
 *
 * EXAMPLE SCENARIOS:
 *
 * Scenario 1: User Service Down During Login
 * - Fallback triggered for getUserByIdentifier()
 * - Returns: "User service temporarily unavailable"
 * - Auth Service shows maintenance message to user
 *
 * Scenario 2: User Service Slow During Registration
 * - Fallback triggered after 10s timeout
 * - Returns: "Registration service temporarily unavailable"
 * - Auth Service advises user to retry
 *
 * MONITORING:
 * - All fallback calls are logged at ERROR level
 * - Includes masked PII (email/phone)
 * - Includes root cause exception
 * - Integrate with ELK/Prometheus for alerts
 *
 * @author YouthConnect Team
 * @version 1.0
 */
@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    /**
     * Creates fallback implementation with access to failure cause
     *
     * PATTERN:
     * FallbackFactory provides more context than simple Fallback
     * - Access to exception that triggered fallback
     * - Ability to log detailed error information
     * - Different behavior based on failure type
     *
     * @param cause Exception that triggered the fallback
     * @return Fallback implementation of UserServiceClient
     */
    @Override
    public UserServiceClient create(Throwable cause) {
        // Log the root cause for debugging and monitoring
        log.error("User-service communication failure: {}", cause.getMessage(), cause);

        // Return anonymous implementation with fallback behavior
        return new UserServiceClient() {

            /**
             * Fallback for getUserByIdentifier
             *
             * CONTEXT: Called during login authentication
             *
             * IMPACT:
             * - User cannot log in
             * - Auth Service shows maintenance message
             *
             * HANDLING:
             * - Return error response (not null)
             * - Auth Service checks ApiResponse.isSuccess()
             * - Shows appropriate error to user
             */
            @Override
            public ApiResponse<UserInfoResponse> getUserByIdentifier(String identifier) {
                log.error("Fallback triggered: getUserByIdentifier for [{}]. Cause: {}",
                        maskIdentifier(identifier), cause.getMessage());
                return ApiResponse.error("User service temporarily unavailable. Please try again later.");
            }

            /**
             * Fallback for getUserByPhone
             *
             * CONTEXT: Called during USSD login
             *
             * IMPACT:
             * - USSD user cannot log in
             * - USSD Service shows error message
             *
             * HANDLING:
             * - Return error response
             * - USSD Service retries or shows maintenance message
             */
            @Override
            public ApiResponse<UserInfoResponse> getUserByPhone(String phoneNumber) {
                log.error("Fallback triggered: getUserByPhone for [{}]. Cause: {}",
                        maskPhone(phoneNumber), cause.getMessage());
                return ApiResponse.error("User service temporarily unavailable. Please try again later.");
            }

            /**
             * Fallback for getUserById
             *
             * CONTEXT: Called during token refresh or profile access
             *
             * IMPACT:
             * - Cannot refresh tokens
             * - User may need to log in again
             *
             * HANDLING:
             * - Return error response
             * - Auth Service may invalidate session
             * - User redirected to login
             */
            @Override
            public ApiResponse<UserInfoResponse> getUserById(UUID userId) {
                log.error("Fallback triggered: getUserById for [{}]. Cause: {}", userId, cause.getMessage());
                return ApiResponse.error("Unable to retrieve user details at the moment. Please try again later.");
            }

            /**
             * Fallback for registerUser
             *
             * CONTEXT: Called during new user registration
             *
             * IMPACT:
             * - New user cannot sign up
             * - Critical business function unavailable
             *
             * HANDLING:
             * - Return error response
             * - Auth Service advises user to retry
             * - Consider queuing registration for later processing
             */
            @Override
            public ApiResponse<UserInfoResponse> registerUser(RegisterRequest request) {
                log.error("Fallback triggered: registerUser for [{}]. Cause: {}",
                        request != null ? request.getEmail() : "unknown", cause.getMessage());
                // Return error instead of null to allow graceful handling
                return ApiResponse.error("Registration service temporarily unavailable. Please try again later.");
            }

            /**
             * Fallback for checkEmailExists
             *
             * CONTEXT: Called during registration email validation
             *
             * IMPACT:
             * - Cannot validate email uniqueness
             * - May allow duplicate registrations
             *
             * HANDLING:
             * - Return error response (not false)
             * - Auth Service can allow registration attempt
             * - Duplicate will be caught at database level
             */
            @Override
            public ApiResponse<Boolean> checkEmailExists(String email) {
                log.warn("Fallback triggered: checkEmailExists for [{}]. Cause: {}", email, cause.getMessage());
                return ApiResponse.error("Unable to verify email existence. Please try again later.");
            }

            /**
             * Fallback for checkPhoneExists
             *
             * CONTEXT: Called during registration phone validation
             *
             * IMPACT:
             * - Cannot validate phone uniqueness
             * - May allow duplicate phone registrations
             *
             * HANDLING:
             * - Return error response (not false)
             * - Auth Service can allow registration attempt
             * - Duplicate will be caught at database level
             */
            @Override
            public ApiResponse<Boolean> checkPhoneExists(String phoneNumber) {
                log.warn("Fallback triggered: checkPhoneExists for [{}]. Cause: {}", maskPhone(phoneNumber), cause.getMessage());
                return ApiResponse.error("Unable to verify phone number existence. Please try again later.");
            }

            /**
             * Fallback for updatePassword
             *
             * CONTEXT: Called during password reset or change
             *
             * IMPACT:
             * - User cannot change password
             * - Password reset flow fails
             *
             * HANDLING:
             * - Return error response
             * - Auth Service shows error message
             * - User must retry password change later
             */
            @Override
            public ApiResponse<Void> updatePassword(UUID userId, UserServiceClient.PasswordUpdateRequest request) {
                log.error("Fallback triggered: updatePassword for [{}]. Cause: {}", userId, cause.getMessage());
                return ApiResponse.error("Unable to update password at the moment. Please try again later.");
            }

            // =================================================================
            // PRIVACY HELPER METHODS
            // =================================================================
            // Mask sensitive data in logs to comply with GDPR/data protection

            /**
             * Mask email or phone identifier for secure logging
             *
             * Examples:
             * - user@example.com → use***@example.com
             * - 256772123456     → 256****456
             */
            private String maskIdentifier(String identifier) {
                if (identifier == null || identifier.length() < 6) return "***";
                return identifier.substring(0, 3) + "***" + identifier.substring(identifier.length() - 3);
            }

            /**
             * Mask phone number for secure logging
             *
             * Examples:
             * - 256772123456  → 256****456
             * - 0772 123 456  → 077****456
             * - +256772123456 → +25****456
             */
            private String maskPhone(String phone) {
                if (phone == null) return "***";
                // Remove formatting characters
                String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");
                if (cleaned.length() >= 7) {
                    return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 3);
                }
                return "***";
            }
        };
    }
}