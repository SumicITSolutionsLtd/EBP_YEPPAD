package com.youthconnect.auth_service.client;

import com.youthconnect.auth_service.dto.request.RegisterRequest;
import com.youthconnect.auth_service.dto.response.UserInfoResponse;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client for User Service Communication
 *
 * Calls internal endpoints on user-service to:
 * - Retrieve user data for authentication
 * - Register new users
 * - Validate user existence
 *
 * @author Youth Connect Uganda Development Team
 */
@FeignClient(
        name = "user-service",
        path = "/api/v1/users/internal",
        fallbackFactory = UserServiceClientFallbackFactory.class
)
public interface UserServiceClient {

    /**
     * Get user by email or phone (for login)
     */
    @GetMapping("/by-identifier")
    ApiResponse<UserInfoResponse> getUserByIdentifier(@RequestParam String identifier);

    /**
     * Get user by phone number (for USSD login)
     */
    @GetMapping("/by-phone")
    ApiResponse<UserInfoResponse> getUserByPhone(@RequestParam String phoneNumber);

    /**
     * Register new user
     */
    @PostMapping("/register")
    ApiResponse<UserInfoResponse> registerUser(@RequestBody RegisterRequest request);

    /**
     * Check if email exists
     */
    @GetMapping("/exists/email")
    ApiResponse<Boolean> checkEmailExists(@RequestParam String email);

    /**
     * Check if phone exists
     */
    @GetMapping("/exists/phone")
    ApiResponse<Boolean> checkPhoneExists(@RequestParam String phoneNumber);
}