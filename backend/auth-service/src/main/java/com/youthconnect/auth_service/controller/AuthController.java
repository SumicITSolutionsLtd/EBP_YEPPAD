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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * Handles registration, login, token management, and security checks.
 *
 * CRITICAL CONFIGURATION:
 * Base Path: /api/auth
 * Matches Gateway Route: - Path=/api/auth/**
 */
@Slf4j
@RestController
// ✅ CRITICAL FIX: Matches the API Gateway "Path" predicate exactly
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*") // Allow direct access if bypassing Gateway (Testing)
@Tag(name = "Authentication", description = "Endpoints for User Registration, Login, and Token Management")
public class AuthController {

    private final AuthService authService;

    // =========================================================================
    // 1. REGISTRATION
    // =========================================================================
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register New User", description = "Creates a new user account with Email or Phone.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed (Invalid email/phone/password)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("📝 Registration attempt for: {}", maskEmail(request.getEmail()));

        try {
            AuthResponse authResponse = authService.register(request);
            log.info("✅ User registered successfully. ID: {}", authResponse.getUserId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(authResponse, "Registration successful"));

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Registration validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (RuntimeException e) {
            // Check for specific duplicates (naive check, better handled by specific exceptions)
            if (e.getMessage() != null && (e.getMessage().toLowerCase().contains("exist") || e.getMessage().toLowerCase().contains("duplicate"))) {
                log.warn("⚠️ Duplicate user registration: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error(e.getMessage()));
            }
            log.error("❌ Registration failed (Runtime): {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed. Please try again."));
        }
    }

    // =========================================================================
    // 2. LOGIN (WEB/MOBILE APP)
    // =========================================================================
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "User Login", description = "Authenticates user and returns JWT Access & Refresh tokens.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("🔐 Login attempt for identifier: {}", maskIdentifier(request.getIdentifier()));

        try {
            AuthResponse authResponse = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));

        } catch (Exception e) {
            log.error("❌ Login failed: {}", e.getMessage());
            // Security Best Practice: Don't reveal exactly why login failed (User vs Password)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email/phone or password"));
        }
    }

    // =========================================================================
    // 3. USSD LOGIN (PHONE ONLY)
    // =========================================================================
    @PostMapping(value = "/ussd/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "USSD Authentication", description = "Simplified login for USSD menu interaction.")
    public ResponseEntity<ApiResponse<AuthResponse>> ussdLogin(@Valid @RequestBody UssdLoginRequest request) {
        log.info("📱 USSD Login attempt for: {}", maskPhoneNumber(request.getPhoneNumber()));

        try {
            AuthResponse authResponse = authService.loginUssd(request);
            return ResponseEntity.ok(ApiResponse.success(authResponse, "USSD Login successful"));
        } catch (Exception e) {
            log.warn("⚠️ USSD Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found or PIN incorrect"));
        }
    }

    // =========================================================================
    // 4. REFRESH TOKEN
    // =========================================================================
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Refresh Access Token", description = "Uses a valid Refresh Token to obtain a new Access Token.")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("🔄 Refreshing token...");

        try {
            AuthResponse authResponse = authService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));
        } catch (Exception e) {
            log.error("❌ Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired refresh token"));
        }
    }

    // =========================================================================
    // 5. LOGOUT
    // =========================================================================
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidates the Refresh Token and clears security context.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) RefreshTokenRequest request) {

        try {
            String accessToken = (authHeader != null && authHeader.startsWith("Bearer "))
                    ? authHeader.substring(7)
                    : null;

            String refreshToken = (request != null) ? request.getRefreshToken() : null;

            if (accessToken == null && refreshToken == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No token provided for logout"));
            }

            authService.logout(accessToken, refreshToken);
            log.info("👋 Logout processed.");

            return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));

        } catch (Exception e) {
            log.error("❌ Logout error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Logout failed"));
        }
    }

    // =========================================================================
    // 6. VALIDATE TOKEN (INTERNAL USE)
    // =========================================================================
    @GetMapping("/validate")
    @Operation(summary = "Validate Token", description = "Checks if the provided Access Token is valid and not expired.")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(ApiResponse.success(false, "Missing Bearer token"));
            }

            String token = authHeader.substring(7);
            boolean isValid = authService.validateToken(token);

            return ResponseEntity.ok(ApiResponse.success(isValid, isValid ? "Token is valid" : "Token is invalid"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success(false, "Token validation error"));
        }
    }

    // =========================================================================
    // 7. HEALTH CHECK
    // =========================================================================
    @GetMapping("/health")
    @Operation(summary = "Service Health", description = "Simple connectivity check.")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("UP", "Auth Service is running"));
    }

    // =========================================================================
    // EXCEPTION HANDLER (VALIDATION)
    // =========================================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("⚠️ Input validation failed: {}", errors);
        return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed", errors));
    }

    // =========================================================================
    // PRIVATE HELPERS FOR SECURE LOGGING
    // =========================================================================

    private String maskIdentifier(String identifier) {
        if (identifier == null) return "null";
        if (identifier.contains("@")) return maskEmail(identifier);
        return maskPhoneNumber(identifier);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) return "***@" + parts[1];
        return parts[0].substring(0, 3) + "***@" + parts[1];
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 2);
    }
}