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
 * ═══════════════════════════════════════════════════════════════════════════
 * INTERNAL USER CONTROLLER - SERVICE-TO-SERVICE API
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Internal REST API for microservice-to-microservice communication.
 * Provides user lookup, registration, validation, and permission checking.
 *
 * <h2>Security - CRITICAL:</h2>
 * <ul>
 *   <li>Only accessible from internal network/VPC</li>
 *   <li>API Gateway MUST block external access to /api/v1/internal/** endpoints</li>
 *   <li>Consider implementing service authentication tokens (JWT/mTLS)</li>
 *   <li>Do NOT expose this controller in public API documentation</li>
 * </ul>
 *
 * <h2>Primary Use Cases:</h2>
 * <ul>
 *   <li>Job-service validates user permissions before job posting</li>
 *   <li>Job-service fetches user/organization info for job listings</li>
 *   <li>Auth-service registers new users (web/USSD)</li>
 *   <li>Other services validate user existence and status</li>
 * </ul>
 *
 * @author Douglas Kings Kato & Youth Connect Uganda Development Team
 * @version 2.1.0
 * @since 2025-10-31
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
@Tag(name = "Internal User API", description = "Internal service-to-service endpoints")
@Hidden // Hide from public Swagger documentation
public class InternalUserController {

    private final UserService userService;

    // =========================================================================
    // USER LOOKUP & RETRIEVAL
    // =========================================================================

    /**
     * Get user by ID
     *
     * @param userId User ID to fetch
     * @return User information response
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Fetch complete user information by user ID")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserById(@PathVariable UUID userId) {
        log.debug("Internal API: getUserById {}", userId);

        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(
                    ApiResponse.success(convertToResponse(user), "User found")
            );
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
        }
    }

    /**
     * Get user by email or phone (auto-detect)
     *
     * @param identifier Email address or phone number
     * @return User information response
     */
    @GetMapping("/by-identifier")
    @Operation(summary = "Get user by email or phone", description = "Auto-detects identifier type and fetches user")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserByIdentifier(@RequestParam String identifier) {
        log.debug("Internal API: getUserByIdentifier {}", maskIdentifier(identifier));

        try {
            User user = identifier.contains("@")
                    ? userService.getUserByEmail(identifier)
                    : userService.getUserByPhone(cleanPhoneNumber(identifier));

            if (user == null) {
                throw new UserNotFoundException("User not found with identifier: " + maskIdentifier(identifier));
            }

            return ResponseEntity.ok(
                    ApiResponse.success(convertToResponse(user), "User found")
            );
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get user by phone number
     *
     * @param phoneNumber Phone number (will be cleaned and normalized)
     * @return User information response
     */
    @GetMapping("/by-phone")
    @Operation(summary = "Get user by phone number", description = "Fetch user by phone number")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserByPhone(@RequestParam String phoneNumber) {
        log.debug("Internal API: getUserByPhone {}", maskPhoneNumber(phoneNumber));

        try {
            User user = userService.getUserByPhone(cleanPhoneNumber(phoneNumber));
            if (user == null) {
                throw new UserNotFoundException("User not found with phone: " + maskPhoneNumber(phoneNumber));
            }

            return ResponseEntity.ok(
                    ApiResponse.success(convertToResponse(user), "User found")
            );
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get user profile summary (lightweight)
     *
     * @param userId User ID to fetch
     * @return UserProfileSummary with essential information
     */
    @GetMapping("/{userId}/summary")
    @Operation(summary = "Get user summary", description = "Fetch lightweight user profile summary for job listings")
    public ResponseEntity<UserProfileSummary> getUserSummary(@PathVariable UUID userId) {
        log.debug("Internal API: Fetching user summary for: {}", userId);
        UserProfileSummary summary = userService.getUserSummary(userId);
        return ResponseEntity.ok(summary);
    }

    // =========================================================================
    // USER REGISTRATION
    // =========================================================================

    /**
     * Register new user (standard web registration)
     *
     * @param request Registration request with user details
     * @return Created user information
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create new user account via web/app registration")
    public ResponseEntity<ApiResponse<UserInfoResponse>> registerUser(
            @Valid @RequestBody RegistrationRequest request) {

        log.info("Internal API: Registering user {}", request.getEmail());

        try {
            User user = userService.createUser(request);
            userService.createUserProfile(user, request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            convertToResponse(user),
                            "User registered successfully"
                    ));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("User already exists with provided email or phone"));
        }
    }

    /**
     * Register USSD user (simplified registration via USSD)
     *
     * @param request USSD registration request
     * @return Created user information
     */
    @PostMapping("/register-ussd")
    @Operation(summary = "Register USSD user", description = "Create user account via USSD channel")
    public ResponseEntity<ApiResponse<UserInfoResponse>> registerUssdUser(
            @Valid @RequestBody UssdRegistrationRequest request) {

        log.info("Internal API: Registering USSD user {}", maskPhoneNumber(request.getPhoneNumber()));

        try {
            RegistrationRequest converted = convertUssdToRegistration(request);
            User user = userService.createUser(converted);
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
    // VALIDATION & EXISTENCE CHECKS
    // =========================================================================

    /**
     * Check if user exists by ID
     *
     * @param userId User ID to check
     * @return Boolean indicating existence
     */
    @GetMapping("/{userId}/exists")
    @Operation(summary = "Check if user exists", description = "Pre-flight check to verify user existence")
    public ResponseEntity<Boolean> userExists(@PathVariable UUID userId) {
        log.debug("Internal API: Checking if user exists: {}", userId);
        boolean exists = userService.userExists(userId);
        return ResponseEntity.ok(exists);
    }

    /**
     * Check if email exists
     *
     * @param email Email address to check
     * @return ApiResponse with boolean result
     */
    @GetMapping("/exists/email")
    @Operation(summary = "Check if email exists", description = "Validate email availability during registration")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
        log.debug("Internal API: Checking email existence: {}", maskEmail(email));
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(
                ApiResponse.success(exists, exists ? "Email exists" : "Email available")
        );
    }

    /**
     * Check if phone exists
     *
     * @param phoneNumber Phone number to check
     * @return ApiResponse with boolean result
     */
    @GetMapping("/exists/phone")
    @Operation(summary = "Check if phone exists", description = "Validate phone availability during registration")
    public ResponseEntity<ApiResponse<Boolean>> checkPhoneExists(@RequestParam String phoneNumber) {
        log.debug("Internal API: Checking phone existence: {}", maskPhoneNumber(phoneNumber));
        boolean exists = userService.phoneExists(cleanPhoneNumber(phoneNumber));
        return ResponseEntity.ok(
                ApiResponse.success(exists, exists ? "Phone exists" : "Phone available")
        );
    }

    // =========================================================================
    // PERMISSIONS & AUTHORIZATION
    // =========================================================================

    /**
     * Check if user can post jobs
     *
     * @param userId User ID to check
     * @return Boolean indicating permission
     */
    @GetMapping("/{userId}/can-post-jobs")
    @Operation(summary = "Check job posting permission", description = "Authorization check before allowing job creation")
    public ResponseEntity<Boolean> canUserPostJobs(@PathVariable UUID userId) {
        log.debug("Internal API: Checking job posting permission for: {}", userId);
        boolean canPost = userService.canUserPostJobs(userId);
        return ResponseEntity.ok(canPost);
    }

    /**
     * Get user's organization name
     *
     * @param userId User ID to fetch organization for
     * @return Organization name
     */
    @GetMapping("/{userId}/organization")
    @Operation(summary = "Get user organization", description = "Fetch organization name for job listing display")
    public ResponseEntity<String> getUserOrganization(@PathVariable UUID userId) {
        log.debug("Internal API: Fetching organization for: {}", userId);
        String organization = userService.getUserOrganization(userId);
        return ResponseEntity.ok(organization);
    }

    // =========================================================================
    // HEALTH CHECK
    // =========================================================================

    /**
     * Health check endpoint
     *
     * @return Health status
     */
    @GetMapping("/health")
    @Operation(summary = "Internal API health check", description = "Verify internal API availability")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponse.success("User service internal API is healthy", "OK")
        );
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Convert User entity to UserInfoResponse DTO
     */
    private UserInfoResponse convertToResponse(User user) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Convert USSD registration to standard registration request
     */
    private RegistrationRequest convertUssdToRegistration(UssdRegistrationRequest ussd) {
        RegistrationRequest req = new RegistrationRequest();
        req.setPhoneNumber(cleanPhoneNumber(ussd.getPhoneNumber()));
        req.setFirstName(ussd.getFirstName());
        req.setLastName(ussd.getLastName());
        req.setGender(ussd.getGender());
        req.setEmail(generateEmailFromPhone(ussd.getPhoneNumber()));
        req.setRole(Role.YOUTH);
        return req;
    }

    /**
     * Generate synthetic email from phone number for USSD users
     */
    private String generateEmailFromPhone(String phone) {
        String cleaned = phone.replaceAll("[^0-9]", "");
        return "ussd_" + cleaned + "@youthconnect.ug";
    }

    /**
     * Clean and normalize phone number to Uganda format (256XXXXXXXXX)
     */
    private String cleanPhoneNumber(String phone) {
        if (phone == null) return null;
        String cleaned = phone.replaceAll("[^+0-9]", "");

        if (cleaned.startsWith("0")) {
            return "256" + cleaned.substring(1);
        } else if (cleaned.startsWith("+256")) {
            return cleaned.substring(1);
        } else if (!cleaned.startsWith("256")) {
            return "256" + cleaned;
        }
        return cleaned;
    }

    /**
     * Mask identifier (email or phone) for logging
     */
    private String maskIdentifier(String identifier) {
        if (identifier == null) return "***";
        return identifier.contains("@") ? maskEmail(identifier) : maskPhoneNumber(identifier);
    }

    /**
     * Mask email address for secure logging
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        if (parts[0].length() > 3) {
            return parts[0].substring(0, 2) + "***@" + parts[1];
        }
        return "***@" + parts[1];
    }

    /**
     * Mask phone number for secure logging
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null) return "***";
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");
        if (cleaned.length() >= 6) {
            return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 3);
        }
        return "***";
    }

    // =========================================================================
    // INTERNAL DTOs
    // =========================================================================

    /**
     * User Profile Summary DTO
     *
     * Lightweight DTO containing essential user information for job-service.
     * Excludes sensitive data like passwords and personal details.
     */
    @Data
    @AllArgsConstructor
    public static class UserProfileSummary {
        /** User's unique identifier */
        private UUID userId;

        /** User's email address */
        private String email;

        /**
         * Full name or organization name
         * Format depends on role:
         * - YOUTH/MENTOR: "FirstName LastName"
         * - NGO/COMPANY: Organization name
         */
        private String fullName;

        /** User's role in the system (for permission checks) */
        private String role;

        /**
         * Organization name (for job posters)
         * Null for individual users (YOUTH, MENTOR)
         */
        private String organizationName;

        /** Account active status */
        private boolean isActive;

        /** Email verification status */
        private boolean emailVerified;
    }
}