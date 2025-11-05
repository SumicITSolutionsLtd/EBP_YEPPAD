package com.youthconnect.auth_service.client;

import com.youthconnect.auth_service.dto.request.RegisterRequest;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.dto.response.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Feign Client for User Service Communication
 *
 * UPDATED: Fixed type parameter to use UUID consistently
 *
 * Handles inter-service communication with user-service for:
 * - User retrieval by various identifiers
 * - User registration
 * - Email/phone existence checks
 * - Password updates
 *
 * Features:
 * - Service discovery via Eureka
 * - Circuit breaker fallback support
 * - Automatic retry on failures
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@FeignClient(
        name = "user-service",
        path = "/api/v1/users/internal",
        fallbackFactory = UserServiceClientFallbackFactory.class
)
public interface UserServiceClient {

    /**
     * Get user by identifier (email or phone)
     *
     * @param identifier Email or phone number
     * @return User information
     */
    @GetMapping("/by-identifier")
    ApiResponse<UserInfoResponse> getUserByIdentifier(@RequestParam("identifier") String identifier);

    /**
     * Get user by phone number
     *
     * @param phoneNumber Phone number in Uganda format
     * @return User information
     */
    @GetMapping("/by-phone")
    ApiResponse<UserInfoResponse> getUserByPhone(@RequestParam("phoneNumber") String phoneNumber);

    /**
     * Get user by UUID
     *
     * @param userId User UUID
     * @return User information
     */
    @GetMapping("/{userId}")
    ApiResponse<UserInfoResponse> getUserById(@PathVariable("userId") UUID userId);

    /**
     * Register new user
     *
     * @param request Registration details
     * @return Created user information
     */
    @PostMapping("/register")
    ApiResponse<UserInfoResponse> registerUser(@RequestBody RegisterRequest request);

    /**
     * Check if email exists
     *
     * @param email Email to check
     * @return True if email exists
     */
    @GetMapping("/exists/email")
    ApiResponse<Boolean> checkEmailExists(@RequestParam("email") String email);

    /**
     * Check if phone number exists
     *
     * @param phoneNumber Phone number to check
     * @return True if phone exists
     */
    @GetMapping("/exists/phone")
    ApiResponse<Boolean> checkPhoneExists(@RequestParam("phoneNumber") String phoneNumber);

    /**
     * Update user password
     * Required for password reset functionality
     *
     * @param userId User UUID
     * @param request New password hash
     * @return Success response
     */
    @PutMapping("/{userId}/password")
    ApiResponse<Void> updatePassword(
            @PathVariable("userId") UUID userId,
            @RequestBody PasswordUpdateRequest request
    );

    /**
     * Password Update Request DTO
     * Used internally for password reset
     */
    record PasswordUpdateRequest(String newPasswordHash) {}
}