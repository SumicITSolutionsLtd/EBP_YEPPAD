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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST Controller
 * Handles all authentication endpoints for the platform
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and Authorization APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user with email/phone and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for: {}", request.getIdentifier());
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @PostMapping("/ussd/login")
    @Operation(summary = "USSD Login", description = "Authenticate USSD user with phone number")
    public ResponseEntity<ApiResponse<AuthResponse>> ussdLogin(@Valid @RequestBody UssdLoginRequest request) {
        log.info("USSD login request received");
        AuthResponse authResponse = authService.loginUssd(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "USSD login successful"));
    }

    @PostMapping("/register")
    @Operation(summary = "User Registration", description = "Register new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for: {}", request.getEmail());
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authResponse, "Registration successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Access Token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request received");
        AuthResponse authResponse = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed"));
    }

    @PostMapping("/logout")
    @Operation(summary = "User Logout", description = "Invalidate access and refresh tokens")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) RefreshTokenRequest request) {

        String token = authHeader.replace("Bearer ", "");
        String refreshToken = request != null ? request.getRefreshToken() : null;

        authService.logout(token, refreshToken);
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate Token", description = "Check if access token is valid")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(isValid, "Token validation complete"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("UP", "Auth service is healthy"));
    }
}
