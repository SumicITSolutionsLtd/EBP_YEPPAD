package com.youthconnect.auth_service.client;

import com.youthconnect.auth_service.dto.request.RegisterRequest;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.dto.response.UserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fallback Factory for UserServiceClient
 *
 * Provides a graceful degradation strategy when the user-service
 * is unavailable or encounters network/timeout issues.
 * <p>
 * Critical operations (e.g., login, registration) will return
 * descriptive error responses while ensuring sensitive data
 * is not exposed in logs.
 * </p>
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        log.error("User-service communication failure: {}", cause.getMessage(), cause);

        return new UserServiceClient() {

            @Override
            public ApiResponse<UserInfoResponse> getUserByIdentifier(String identifier) {
                log.error("Fallback triggered: getUserByIdentifier for [{}]. Cause: {}",
                        maskIdentifier(identifier), cause.getMessage());
                return ApiResponse.error("User service temporarily unavailable. Please try again later.");
            }

            @Override
            public ApiResponse<UserInfoResponse> getUserByPhone(String phoneNumber) {
                log.error("Fallback triggered: getUserByPhone for [{}]. Cause: {}",
                        maskPhone(phoneNumber), cause.getMessage());
                return ApiResponse.error("User service temporarily unavailable. Please try again later.");
            }

            @Override
            public ApiResponse<UserInfoResponse> getUserById(UUID userId) {
                log.error("Fallback triggered: getUserById for [{}]. Cause: {}", userId, cause.getMessage());
                return ApiResponse.error("Unable to retrieve user details at the moment. Please try again later.");
            }

            @Override
            public ApiResponse<UserInfoResponse> registerUser(RegisterRequest request) {
                log.error("Fallback triggered: registerUser for [{}]. Cause: {}",
                        request != null ? request.getEmail() : "unknown", cause.getMessage());
                return ApiResponse.error("Registration service temporarily unavailable. Please try again later.");
            }

            @Override
            public ApiResponse<Boolean> checkEmailExists(String email) {
                log.warn("Fallback triggered: checkEmailExists for [{}]. Cause: {}", email, cause.getMessage());
                return ApiResponse.error("Unable to verify email existence. Please try again later.");
            }

            @Override
            public ApiResponse<Boolean> checkPhoneExists(String phoneNumber) {
                log.warn("Fallback triggered: checkPhoneExists for [{}]. Cause: {}", maskPhone(phoneNumber), cause.getMessage());
                return ApiResponse.error("Unable to verify phone number existence. Please try again later.");
            }

            @Override
            public ApiResponse<Void> updatePassword(UUID userId, UserServiceClient.PasswordUpdateRequest request) {
                log.error("Fallback triggered: updatePassword for [{}]. Cause: {}", userId, cause.getMessage());
                return ApiResponse.error("Unable to update password at the moment. Please try again later.");
            }

            /**
             * Mask identifier (email or phone) for privacy in logs.
             */
            private String maskIdentifier(String identifier) {
                if (identifier == null || identifier.length() < 6) return "***";
                return identifier.substring(0, 3) + "***" + identifier.substring(identifier.length() - 3);
            }

            /**
             * Mask phone number for privacy in logs.
             */
            private String maskPhone(String phone) {
                if (phone == null) return "***";
                String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");
                if (cleaned.length() >= 7) {
                    return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 3);
                }
                return "***";
            }
        };
    }
}