package com.youthconnect.auth_service.client;

import com.youthconnect.auth_service.dto.request.RegisterRequest;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.dto.response.UserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback factory for UserServiceClient
 * Provides graceful degradation when user-service is unavailable
 */
@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        log.error("User service unavailable: {}", cause.getMessage());

        return new UserServiceClient() {
            @Override
            public ApiResponse<UserInfoResponse> getUserByIdentifier(String identifier) {
                log.error("Fallback: getUserByIdentifier called for {}", identifier);
                throw new RuntimeException("User service unavailable. Please try again later.");
            }

            @Override
            public ApiResponse<UserInfoResponse> getUserByPhone(String phoneNumber) {
                log.error("Fallback: getUserByPhone called");
                throw new RuntimeException("User service unavailable. Please try again later.");
            }

            @Override
            public ApiResponse<UserInfoResponse> registerUser(RegisterRequest request) {
                log.error("Fallback: registerUser called for {}", request.getEmail());
                throw new RuntimeException("User service unavailable. Please try again later.");
            }

            @Override
            public ApiResponse<Boolean> checkEmailExists(String email) {
                log.error("Fallback: checkEmailExists called for {}", email);
                return ApiResponse.error("User service unavailable");
            }

            @Override
            public ApiResponse<Boolean> checkPhoneExists(String phoneNumber) {
                log.error("Fallback: checkPhoneExists called");
                return ApiResponse.error("User service unavailable");
            }
        };
    }
}