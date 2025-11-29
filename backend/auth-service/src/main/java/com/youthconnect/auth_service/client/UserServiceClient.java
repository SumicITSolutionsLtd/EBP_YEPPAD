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
 * PURPOSE:
 * Provides type-safe HTTP client for Auth Service to communicate with User Service.
 * All requests go through the INTERNAL API endpoints protected by network rules.
 *
 * CONFIGURATION:
 * - Service Name: "user-service" (Resolved via Eureka/Kubernetes DNS)
 * - Base Path: /api/v1/internal/users (Matches InternalUserController)
 * - Fallback: UserServiceClientFallbackFactory (Circuit Breaker)
 *
 * @author YouthConnect Team
 * @see UserServiceClientFallbackFactory
 */
@FeignClient(
        name = "user-service",                          // Service discovery name
        path = "/api/v1/internal/users",                // Base path matching User Service InternalController
        fallbackFactory = UserServiceClientFallbackFactory.class  // Circuit breaker fallback
)
public interface UserServiceClient {

    /**
     * Get user by email or phone number (flexible identifier)
     *
     * Used during Login to fetch user credentials (password hash).
     *
     * Target: GET /api/v1/internal/users/by-identifier
     */
    @GetMapping("/by-identifier")
    ApiResponse<UserInfoResponse> getUserByIdentifier(@RequestParam("identifier") String identifier);

    /**
     * Get user by phone number (strict phone lookup)
     *
     * Used for USSD login validation and phone verification.
     *
     * Target: GET /api/v1/internal/users/by-phone
     */
    @GetMapping("/by-phone")
    ApiResponse<UserInfoResponse> getUserByPhone(@RequestParam("phoneNumber") String phoneNumber);

    /**
     * Get user by UUID
     *
     * Used for Token Refresh and fetching latest profile data.
     *
     * Target: GET /api/v1/internal/users/{userId}
     */
    @GetMapping("/{userId}")
    ApiResponse<UserInfoResponse> getUserById(@PathVariable("userId") UUID userId);

    /**
     * Register new user (Web/App registration)
     *
     * This endpoints saves the user data to the User Database.
     * The password must be hashed by Auth Service BEFORE sending.
     *
     * Target: POST /api/v1/internal/users/register
     *
     * @param request Contains user details + hashed password
     * @return Created user info
     */
    @PostMapping("/register")
    ApiResponse<UserInfoResponse> registerUser(@RequestBody RegisterRequest request);

    /**
     * Check if email already exists
     *
     * Used for pre-registration validation.
     *
     * Target: GET /api/v1/internal/users/exists/email
     */
    @GetMapping("/exists/email")
    ApiResponse<Boolean> checkEmailExists(@RequestParam("email") String email);

    /**
     * Check if phone number already exists
     *
     * Used for pre-registration validation.
     *
     * Target: GET /api/v1/internal/users/exists/phone
     */
    @GetMapping("/exists/phone")
    ApiResponse<Boolean> checkPhoneExists(@RequestParam("phoneNumber") String phoneNumber);

    /**
     * Update user password
     *
     * Used for Password Reset or Change Password flows.
     * Auth Service must hash the new password before calling this.
     *
     * Target: PUT /api/v1/internal/users/{userId}/password
     */
    @PutMapping("/{userId}/password")
    ApiResponse<Void> updatePassword(
            @PathVariable("userId") UUID userId,
            @RequestBody PasswordUpdateRequest request
    );

    // =========================================================================
    // INNER DTOs
    // =========================================================================

    /**
     * DTO for password update payload
     * @param newPasswordHash The new BCrypt password hash
     */
    record PasswordUpdateRequest(String newPasswordHash) {}
}