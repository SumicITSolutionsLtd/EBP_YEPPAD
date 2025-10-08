package com.youthconnect.user_service.controller;

import com.youthconnect.user_service.dto.response.ApiResponse;
import com.youthconnect.user_service.dto.request.RegistrationRequest;
import com.youthconnect.user_service.dto.request.UssdRegistrationRequest;
import com.youthconnect.user_service.dto.response.UserInfoResponse;
import com.youthconnect.user_service.entity.Role;
import com.youthconnect.user_service.entity.User;
import com.youthconnect.user_service.exception.UserAlreadyExistsException;
import com.youthconnect.user_service.exception.UserNotFoundException;
import com.youthconnect.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal User API Controller
 *
 * SECURITY CRITICAL: For service-to-service communication ONLY
 * Must be secured at API Gateway level
 *
 * @author Youth Connect Uganda Development Team
 * @version 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users/internal")
@RequiredArgsConstructor
@Tag(name = "Internal User API", description = "Internal service-to-service endpoints")
public class InternalUserController {

    private final UserService userService;

    // =========================================================================
    // USER LOOKUP
    // =========================================================================

    @GetMapping("/by-identifier")
    @Operation(summary = "Get user by email or phone")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserByIdentifier(
            @RequestParam String identifier) {

        log.debug("Internal API: getUserByIdentifier {}", maskIdentifier(identifier));

        try {
            User user = identifier.contains("@")
                    ? userService.getUserByEmail(identifier)
                    : userService.getUserByPhone(cleanPhoneNumber(identifier));

            if (user == null) {
                throw new UserNotFoundException("User not found");
            }

            return ResponseEntity.ok(
                    ApiResponse.success(convertToResponse(user), "User found")
            );
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/by-phone")
    @Operation(summary = "Get user by phone number")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserByPhone(
            @RequestParam String phoneNumber) {

        log.debug("Internal API: getUserByPhone {}", maskPhoneNumber(phoneNumber));

        try {
            User user = userService.getUserByPhone(cleanPhoneNumber(phoneNumber));
            if (user == null) throw new UserNotFoundException("User not found");

            return ResponseEntity.ok(
                    ApiResponse.success(convertToResponse(user), "User found")
            );
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserById(
            @PathVariable Long userId) {

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

    // =========================================================================
    // REGISTRATION
    // =========================================================================

    @PostMapping("/register")
    @Operation(summary = "Register new user")
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
                    .body(ApiResponse.error("User already exists"));
        }
    }

    @PostMapping("/register-ussd")
    @Operation(summary = "Register USSD user")
    public ResponseEntity<ApiResponse<UserInfoResponse>> registerUssdUser(
            @Valid @RequestBody UssdRegistrationRequest request) {

        log.info("Internal API: Registering USSD user {}",
                maskPhoneNumber(request.getPhoneNumber()));

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
                    .body(ApiResponse.error("Phone already registered"));
        }
    }

    // =========================================================================
    // VALIDATION
    // =========================================================================

    @GetMapping("/exists/email")
    @Operation(summary = "Check if email exists")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(
            @RequestParam String email) {

        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(
                ApiResponse.success(exists, exists ? "Email exists" : "Email available")
        );
    }

    @GetMapping("/exists/phone")
    @Operation(summary = "Check if phone exists")
    public ResponseEntity<ApiResponse<Boolean>> checkPhoneExists(
            @RequestParam String phoneNumber) {

        boolean exists = userService.phoneExists(cleanPhoneNumber(phoneNumber));
        return ResponseEntity.ok(
                ApiResponse.success(exists, exists ? "Phone exists" : "Phone available")
        );
    }

    // =========================================================================
    // HEALTH
    // =========================================================================

    @GetMapping("/health")
    @Operation(summary = "Internal API health check")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponse.success("User service internal API is healthy", "OK")
        );
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

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

    private String generateEmailFromPhone(String phone) {
        String cleaned = phone.replaceAll("[^0-9]", "");
        return "ussd_" + cleaned + "@youthconnect.ug";
    }

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

    private String maskIdentifier(String identifier) {
        if (identifier == null) return "***";
        return identifier.contains("@")
                ? maskEmail(identifier)
                : maskPhoneNumber(identifier);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        if (parts[0].length() > 3) {
            return parts[0].substring(0, 2) + "***@" + parts[1];
        }
        return "***@" + parts[1];
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null) return "***";
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");
        if (cleaned.length() >= 6) {
            return cleaned.substring(0, 3) + "****" +
                    cleaned.substring(cleaned.length() - 3);
        }
        return "***";
    }
}