package com.youthconnect.auth_service.controller;

import com.youthconnect.auth_service.dto.request.LoginRequest;
import com.youthconnect.auth_service.dto.request.RegisterRequest;
import com.youthconnect.auth_service.dto.request.RefreshTokenRequest;
import com.youthconnect.auth_service.dto.request.UssdLoginRequest;
import com.youthconnect.auth_service.dto.response.AuthResponse;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================================
 *  AUTH CONTROLLER
 * ============================================================================
 * Handles all authentication-related endpoints for the Youth Connect platform.
 *
 * ✅ Supports:
 *   - Web & USSD Login
 *   - User Registration
 *   - Token Refresh
 *   - Token Validation
 *   - Logout
 *   - Health Check
 *
 * This controller returns consistent JSON responses via the {@link ApiResponse} wrapper.
 *
 * @author
 *     Douglas Kings Kato
 * @version
 *     2.0.0 (Refined Edition)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and Authorization APIs")
public class AuthController {

    private final AuthService authService;

    // =========================================================================
    // LOGIN (Web)
    // =========================================================================
    /**
     * Authenticates a user with email/phone and password.
     *
     * @param request Login credentials
     * @return AuthResponse containing JWT tokens
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "User Login", description = "Authenticate user with email/phone and password. Returns JWT tokens.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for: {}", maskIdentifier(request.getIdentifier()));
        AuthResponse authResponse = authService.login(request);
        return ApiResponse.success(authResponse, "Login successful");
    }

    // =========================================================================
    // LOGIN (USSD)
    // =========================================================================
    /**
     * Simplified login for USSD users using only phone number.
     */
    @PostMapping("/ussd/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "USSD Login", description = "Authenticate USSD user with phone number only.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "USSD login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Phone number not registered")
    })
    public ApiResponse<AuthResponse> ussdLogin(@Valid @RequestBody UssdLoginRequest request) {
        log.info("USSD login request received for phone: {}", maskPhoneNumber(request.getPhoneNumber()));
        AuthResponse authResponse = authService.loginUssd(request);
        return ApiResponse.success(authResponse, "USSD login successful");
    }

    // =========================================================================
    // REGISTRATION
    // =========================================================================
    /**
     * Registers a new user and automatically logs them in.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "User Registration", description = "Register new user with email/phone and role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Registration successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid registration data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or phone already exists")
    })
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        AuthResponse authResponse = authService.register(request);
        return ApiResponse.success(authResponse, "Registration successful. Welcome to Youth Connect Uganda!");
    }

    // =========================================================================
    // REFRESH TOKEN
    // =========================================================================
    /**
     * Generates a new access token using a valid refresh token.
     */
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Refresh Access Token", description = "Get new access token using refresh token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ApiResponse<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request received");
        AuthResponse authResponse = authService.refreshAccessToken(request.getRefreshToken());
        return ApiResponse.success(authResponse, "Token refreshed successfully");
    }

    // =========================================================================
    // LOGOUT
    // =========================================================================
    /**
     * Logs out the user by revoking refresh token and blacklisting access token.
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "User Logout", description = "Invalidate access and refresh tokens.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid token")
    })
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) RefreshTokenRequest request) {

        // Extract token from "Bearer ..." header
        String token = authHeader.replace("Bearer ", "");
        String refreshToken = request != null ? request.getRefreshToken() : null;

        log.info("Logout request received");
        authService.logout(token, refreshToken);

        return ApiResponse.success(null, "Logout successful");
    }

    // =========================================================================
    // TOKEN VALIDATION
    // =========================================================================
    /**
     * Validates an access token to ensure it’s still valid and not blacklisted.
     */
    @GetMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Validate Token", description = "Check if access token is valid and not blacklisted.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token validation complete"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid token")
    })
    public ApiResponse<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        log.debug("Token validation request received");

        boolean isValid = authService.validateToken(token);
        String message = isValid ? "Token is valid" : "Token is invalid or expired";

        return ApiResponse.success(isValid, message);
    }

    // =========================================================================
    // HEALTH CHECK
    // =========================================================================
    /**
     * Simple public endpoint for health checks and uptime monitoring.
     */
    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Health Check", description = "Check if auth service is running.")
    public ApiResponse<String> health() {
        return ApiResponse.success("UP", "Auth service is healthy");
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================
    /**
     * Masks identifier (email/phone) for privacy in logs.
     */
    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 6) {
            return "***";
        }
        return identifier.substring(0, 3) + "***" + identifier.substring(identifier.length() - 3);
    }

    /**
     * Masks phone number for privacy in logs.
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null) return "***";
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");
        if (cleaned.length() >= 6) {
            return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 3);
        }
        return "***";
    }
}
