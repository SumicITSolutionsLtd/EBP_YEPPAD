package com.youthconnect.auth_service.client;

import com.youthconnect.auth_service.dto.request.RegisterRequest;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.dto.response.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client for User Service Communication
 *
 * Handles all inter-service HTTP communication with {@code user-service} for:
 * <ul>
 *     <li>User registration and profile creation</li>
 *     <li>User lookup by email, phone number, or ID</li>
 *     <li>User data retrieval for authentication and verification</li>
 *     <li>Validation of existing user accounts (email or phone)</li>
 * </ul>
 *
 * Configuration:
 * <ul>
 *     <li>Service discovery via Eureka for dynamic endpoint resolution</li>
 *     <li>Load-balanced calls across user-service instances</li>
 *     <li>Circuit breaker and fallback factory for fault tolerance</li>
 * </ul>
 *
 * Base Path: {@code /api/v1/users/internal}
 *
 * @version 1.2.0
 * @since 2025-10
 * @author
 *     Youth Connect Uganda Development Team
 */
@FeignClient(
        name = "user-service",
        path = "/api/v1/users/internal",
        fallbackFactory = UserServiceClientFallbackFactory.class
)
public interface UserServiceClient {

    /**
     * Retrieve user by email or phone number.
     * <p>
     * Used during login or authentication workflows to obtain user details
     * based on a single identifier (either email or phone number).
     * </p>
     *
     * Endpoint: {@code GET /api/v1/users/internal/by-identifier?identifier={value}}
     *
     * @param identifier Email address or phone number
     * @return ApiResponse containing {@link UserInfoResponse} or error if not found
     */
    @GetMapping("/by-identifier")
    ApiResponse<UserInfoResponse> getUserByIdentifier(@RequestParam("identifier") String identifier);

    /**
     * Retrieve user by phone number only.
     * <p>
     * Commonly used for USSD or SMS-based authentication where only
     * a phone number is available.
     * </p>
     *
     * Endpoint: {@code GET /api/v1/users/internal/by-phone?phoneNumber={value}}
     *
     * @param phoneNumber User’s phone number (international or Ugandan format)
     * @return ApiResponse containing {@link UserInfoResponse} or error if not found
     */
    @GetMapping("/by-phone")
    ApiResponse<UserInfoResponse> getUserByPhone(@RequestParam("phoneNumber") String phoneNumber);

    /**
     * Retrieve complete user details by user ID.
     * <p>
     * Typically used for profile updates, token refresh operations,
     * or internal data synchronization.
     * </p>
     *
     * Endpoint: {@code GET /api/v1/users/internal/{userId}}
     *
     * @param userId User’s unique database identifier
     * @return ApiResponse containing {@link UserInfoResponse}
     */
    @GetMapping("/{userId}")
    ApiResponse<UserInfoResponse> getUserById(@PathVariable("userId") Long userId);

    /**
     * Register a new user.
     * <p>
     * Delegates registration and profile creation to user-service.
     * The service handles validation, persistence, and post-registration setup.
     * </p>
     *
     * Endpoint: {@code POST /api/v1/users/internal/register}
     *
     * @param request Registration request containing user details
     * @return ApiResponse containing the newly created {@link UserInfoResponse}
     */
    @PostMapping("/register")
    ApiResponse<UserInfoResponse> registerUser(@RequestBody RegisterRequest request);

    /**
     * Check if an email address already exists in the system.
     * <p>
     * Useful during registration and account recovery workflows.
     * </p>
     *
     * Endpoint: {@code GET /api/v1/users/internal/exists/email?email={value}}
     *
     * @param email User’s email address
     * @return ApiResponse containing {@code true} if the email exists, {@code false} otherwise
     */
    @GetMapping("/exists/email")
    ApiResponse<Boolean> checkEmailExists(@RequestParam("email") String email);

    /**
     * Check if a phone number already exists in the system.
     * <p>
     * Used for preventing duplicate accounts and validating phone-based users.
     * </p>
     *
     * Endpoint: {@code GET /api/v1/users/internal/exists/phone?phoneNumber={value}}
     *
     * @param phoneNumber User’s phone number
     * @return ApiResponse containing {@code true} if the phone exists, {@code false} otherwise
     */
    @GetMapping("/exists/phone")
    ApiResponse<Boolean> checkPhoneExists(@RequestParam("phoneNumber") String phoneNumber);
}
