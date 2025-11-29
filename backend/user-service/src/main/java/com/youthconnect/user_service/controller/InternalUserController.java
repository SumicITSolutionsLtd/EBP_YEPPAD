package com.youthconnect.user_service.controller;

import com.youthconnect.user_service.dto.request.RegistrationRequest;
import com.youthconnect.user_service.dto.request.UssdRegistrationRequest;
import com.youthconnect.user_service.dto.response.ApiResponse;
import com.youthconnect.user_service.dto.response.UserInfoResponse;
import com.youthconnect.user_service.entity.Role;
import com.youthconnect.user_service.entity.User;
import com.youthconnect.user_service.exception.UserAlreadyExistsException;
import com.youthconnect.user_service.exception.UserNotFoundException;
import com.youthconnect.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal REST API Controller for Microservice-to-Microservice Communication
 *
 * Purpose:
 * - Provides internal endpoints for user lookup, registration, and validation.
 * - Used specifically by Auth-Service, Job-Service, and USSD-Service via Feign clients.
 * - NOT exposed to public clients via the Gateway (protected by firewall/network rules).
 *
 * Base Path: /api/v1/internal/users
 * Matches SecurityConfig: .requestMatchers("/api/v1/internal/**").permitAll()
 *
 * @author YouthConnect Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/internal/users") // ✅ Matches SecurityConfig allow-list
@RequiredArgsConstructor
@Tag(name = "Internal User API", description = "Internal service-to-service endpoints")
@Hidden // ✅ Hides this controller from public Swagger UI
public class InternalUserController {

    private final UserService userService;

    // =========================================================================
    // USER REGISTRATION ENDPOINTS (Called by Auth Service)
    // =========================================================================

    /**
     * Register new user (Web/App registration)
     *
     * Flow:
     * 1. Auth Service receives POST /auth/register
     * 2. Auth Service validates request
     * 3. Auth Service calls this internal endpoint to save User data
     * 4. Returns UserInfoResponse so Auth Service can generate JWT
     *
     * URL: POST /api/v1/internal/users/register
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create new user account via web/app registration")
    public ResponseEntity<ApiResponse<UserInfoResponse>> registerUser(
            @Valid @RequestBody RegistrationRequest request) {

        log.info("Internal API: Registering user email: {}", maskEmail(request.getEmail()));

        try {
            // 1. Create user entity (handles password hashing inside service)
            User user = userService.createUser(request);

            // 2. Create associated user profile
            userService.createUserProfile(user, request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            convertToResponse(user),
                            "User registered successfully"
                    ));
        } catch (UserAlreadyExistsException e) {
            log.warn("Registration failed - User exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("User already exists with provided email or phone"));
        }
    }

    /**
     * Register USSD user (Feature phone registration)
     *
     * URL: POST /api/v1/internal/users/register-ussd
     */
    @PostMapping("/register-ussd")
    @Operation(summary = "Register USSD user", description = "Create user account via USSD (feature phone)")
    public ResponseEntity<ApiResponse<UserInfoResponse>> registerUssdUser(
            @Valid @RequestBody UssdRegistrationRequest request) {

        log.info("Internal API: Registering USSD user phone: {}", maskPhoneNumber(request.getPhoneNumber()));

        try {
            // Convert USSD request to standard registration format
            RegistrationRequest converted = convertUssdToRegistration(request);

            // Create user entity
            User user = userService.createUser(converted);

            // Create USSD-specific profile (simplified)
            userService.createUssdUserProfile(user, request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            convertToResponse(user),
                            "USSD user registered successfully"
                    ));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Phone number already registered"));
        }
    }

    // =========================================================================
    // USER LOOKUP ENDPOINTS (Called by Auth & Job Services)
    // =========================================================================

    /**
     * Get user by UUID
     * URL: GET /api/v1/internal/users/{userId}
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by UUID")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserById(@PathVariable UUID userId) {
        log.debug("Internal API: getUserById {}", userId);
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(ApiResponse.success(convertToResponse(user), "User found"));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
        }
    }

    /**
     * Get user by email OR phone number (flexible identifier)
     * Used by Auth Service during Login
     * URL: GET /api/v1/internal/users/by-identifier?identifier=...
     */
    @GetMapping("/by-identifier")
    @Operation(summary = "Get user by email or phone", description = "Flexible lookup by identifier")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserByIdentifier(
            @RequestParam String identifier) {

        log.debug("Internal API: getUserByIdentifier {}", maskIdentifier(identifier));

        try {
            // Determine if identifier is email or phone based on '@' presence
            User user = identifier.contains("@")
                    ? userService.getUserByEmail(identifier)
                    : userService.getUserByPhone(cleanPhoneNumber(identifier));

            if (user == null) {
                throw new UserNotFoundException("User not found");
            }

            return ResponseEntity.ok(ApiResponse.success(convertToResponse(user), "User found"));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
        }
    }

    /**
     * Get lightweight user summary
     * Used by Job Service to display "Posted by..."
     * URL: GET /api/v1/internal/users/{userId}/summary
     */
    @GetMapping("/{userId}/summary")
    @Operation(summary = "Get user summary", description = "Lightweight user profile for display")
    public ResponseEntity<UserProfileSummary> getUserSummary(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserSummary(userId));
    }

    // =========================================================================
    // VALIDATION ENDPOINTS
    // =========================================================================

    /**
     * Check if email exists
     * URL: GET /api/v1/internal/users/exists/email
     */
    @GetMapping("/exists/email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(ApiResponse.success(exists, exists ? "Email exists" : "Email available"));
    }

    /**
     * Check if phone exists
     * URL: GET /api/v1/internal/users/exists/phone
     */
    @GetMapping("/exists/phone")
    public ResponseEntity<ApiResponse<Boolean>> checkPhoneExists(@RequestParam String phoneNumber) {
        boolean exists = userService.phoneExists(cleanPhoneNumber(phoneNumber));
        return ResponseEntity.ok(ApiResponse.success(exists, exists ? "Phone exists" : "Phone available"));
    }

    /**
     * Health check for K8s/Gateway
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("User service internal API is healthy", "OK"));
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    private UserInfoResponse convertToResponse(User user) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .passwordHash(user.getPasswordHash()) // Crucial for Auth Service verification
                .role(user.getRole().name())
                .isActive(user.isActive())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private RegistrationRequest convertUssdToRegistration(UssdRegistrationRequest ussd) {
        RegistrationRequest req = new RegistrationRequest();
        req.setPhoneNumber(cleanPhoneNumber(ussd.getPhoneNumber()));
        req.setFirstName(ussd.getFirstName());
        req.setLastName(ussd.getLastName());
        req.setGender(ussd.getGender());
        // Auto-generate placeholder email for USSD users to satisfy DB constraints
        String cleanedPhone = req.getPhoneNumber();
        req.setEmail("ussd_" + cleanedPhone + "@youthconnect.ug");
        req.setRole(Role.YOUTH);
        return req;
    }

    private String cleanPhoneNumber(String phone) {
        if (phone == null) return null;
        // Remove non-digits and non-plus
        String cleaned = phone.replaceAll("[^+0-9]", "");
        // Normalize 07... to 2567...
        if (cleaned.startsWith("0")) return "256" + cleaned.substring(1);
        if (cleaned.startsWith("+256")) return cleaned.substring(1);
        if (!cleaned.startsWith("256")) return "256" + cleaned;
        return cleaned;
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null) return "***";
        return identifier.contains("@") ? maskEmail(identifier) : maskPhoneNumber(identifier);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        return parts[0].length() > 3
                ? parts[0].substring(0, 2) + "***@" + parts[1]
                : "***@" + parts[1];
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null) return "***";
        return phone.length() >= 6
                ? phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3)
                : "***";
    }

    // =========================================================================
    // INNER DTO CLASS
    // =========================================================================

    @Data
    @AllArgsConstructor
    public static class UserProfileSummary {
        private UUID userId;
        private String email;
        private String fullName;
        private String role;
        private String organizationName;
        private boolean isActive;
        private boolean emailVerified;
    }
}