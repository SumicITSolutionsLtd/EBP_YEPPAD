package com.youthconnect.ussd_service.client;

import com.youthconnect.ussd_service.dto.UssdRegistrationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign Client for Authentication Service Communication
 *
 * Purpose:
 * =======
 * Enables ussd-service to interact with auth-service for:
 * - User registration (USSD flow)
 * - Phone-based authentication
 * - User validation
 * - Password management
 *
 * Service Communication:
 * =====================
 * - Uses Eureka service discovery
 * - Automatic load balancing
 * - Circuit breaker fallback integration
 * - Request/response logging
 *
 * Authentication Flow:
 * ===================
 * 1. User dials *256#
 * 2. USSD service collects information
 * 3. Sends registration request to auth-service
 * 4. Auth-service creates user + generates tokens
 * 5. Returns authentication response
 * 6. USSD confirms to user
 *
 * Security:
 * ========
 * - All requests include source header
 * - Timeouts configured for resilience
 * - Fallback to graceful degradation
 * - Phone numbers are validated before transmission
 *
 * @author YouthConnect Uganda Development Team
 * @version 1.0.0
 */
@FeignClient(
        name = "auth-service",                    // Service name in Eureka
        path = "/",                                // Base path (API Gateway handles routing)
        fallback = AuthServiceFallback.class       // Fallback on service failure
)
public interface AuthServiceClient {

    /**
     * Registers new USSD user via auth-service
     *
     * Complete Registration Flow:
     * ===========================
     * 1. USSD Service collects: name, gender, age, district, business stage
     * 2. Sends registration request to auth-service
     * 3. Auth-service processes:
     *    a. Validates phone number uniqueness
     *    b. Generates default password: "USSD" + last 4 digits
     *    c. Hashes password with BCrypt
     *    d. Creates user via user-service
     *    e. Generates JWT tokens
     *    f. Sends welcome SMS
     * 4. Returns registration response
     *
     * Default Credentials:
     * ===================
     * - Phone: +256700123456
     * - Password: USSD3456 (last 4 digits)
     * - User can change password later
     *
     * Response Structure:
     * ==================
     * {
     *   "success": true,
     *   "message": "Registration successful",
     *   "userId": 12345,
     *   "accessToken": "eyJhbGc...",
     *   "refreshToken": "eyJhbGc...",
     *   "defaultPassword": "USSD3456"
     * }
     *
     * Error Handling:
     * ==============
     * - 409 Conflict: Phone number already exists
     * - 400 Bad Request: Invalid data
     * - 500 Server Error: Service unavailable (triggers fallback)
     *
     * @param request Registration request with user details
     * @return Registration response with tokens and user info
     */
    @PostMapping("/register")
    ResponseEntity<Map<String, Object>> registerUssdUser(
            @RequestBody UssdRegistrationRequest request
    );

    /**
     * Authenticates USSD user by phone number only
     *
     * USSD Authentication Model:
     * =========================
     * - No password required for USSD sessions
     * - Phone number verified by telecom network
     * - Short-lived session tokens (15 minutes)
     * - Limited to USSD operations only
     *
     * Security Rationale:
     * ==================
     * - Africa's Talking validates phone ownership
     * - USSD is inherently secure (direct telco channel)
     * - Session tokens prevent unauthorized reuse
     * - Tokens expire automatically
     *
     * Use Cases:
     * =========
     * - Returning user dials *256#
     * - System auto-authenticates
     * - Grants access to main menu
     * - No manual login required
     *
     * Request Format:
     * ==============
     * {
     *   "phoneNumber": "+256700123456"
     * }
     *
     * Response Format:
     * ===============
     * {
     *   "success": true,
     *   "accessToken": "eyJhbGc...",
     *   "userId": 12345,
     *   "userName": "John Doe",
     *   "sessionExpiry": "2025-01-29T15:30:00"
     * }
     *
     * @param request Map containing phoneNumber
     * @return Authentication response with session token
     */
    @PostMapping("/ussd/login")
    ResponseEntity<Map<String, Object>> loginUssdUser(
            @RequestBody Map<String, String> request
    );

    /**
     * Checks if phone number is already registered
     *
     * Critical for USSD Flow:
     * ======================
     * - Determines user routing (registration vs main menu)
     * - Prevents duplicate registrations
     * - Validates phone format
     *
     * USSD Menu Logic:
     * ===============
     * IF phone exists:
     *   → Show main menu
     *   → Allow opportunity browsing
     * ELSE:
     *   → Show registration flow
     *   → Collect user details
     *
     * Performance Optimization:
     * ========================
     * - Cached results (5 minutes)
     * - Fast lookup (<100ms)
     * - Minimal database load
     *
     * Request Format:
     * ==============
     * GET /validate/phone?phoneNumber=%2B256700123456
     *
     * Response Format:
     * ===============
     * {
     *   "success": true,
     *   "registered": true,
     *   "userId": 12345,
     *   "registeredDate": "2025-01-15"
     * }
     *
     * @param phoneNumber Phone number to validate (URL encoded)
     * @return Validation response with registration status
     */
    @GetMapping("/validate/phone")
    ResponseEntity<Map<String, Object>> checkPhoneRegistered(
            @RequestParam("phoneNumber") String phoneNumber
    );

    /**
     * Sends password reset SMS to USSD user
     *
     * Password Recovery Flow:
     * ======================
     * 1. User requests password reset via USSD
     * 2. USSD service calls this endpoint
     * 3. Auth-service generates reset token
     * 4. Sends SMS with reset instructions
     * 5. User can reset via web or USSD
     *
     * SMS Content Example:
     * ===================
     * "YouthConnect: Reset your password at
     *  youthconnect.ug/reset?token=abc123
     *  Or dial *256# and select Password Reset.
     *  Token expires in 1 hour."
     *
     * Security Measures:
     * =================
     * - Token valid for 1 hour only
     * - Single use token
     * - Requires phone ownership verification
     * - Rate limited (3 requests per hour)
     *
     * Request Format:
     * ==============
     * {
     *   "phoneNumber": "+256700123456"
     * }
     *
     * @param request Map containing phoneNumber
     * @return Response indicating SMS sent status
     */
    @PostMapping("/ussd/reset-password")
    ResponseEntity<Map<String, Object>> sendPasswordResetSms(
            @RequestBody Map<String, String> request
    );

    /**
     * Validates USSD session token
     *
     * Token Validation Purpose:
     * ========================
     * - Ensures session is still active
     * - Prevents token replay attacks
     * - Validates token signature
     * - Checks expiration time
     *
     * USSD Session Management:
     * =======================
     * - Sessions timeout after 15 minutes
     * - Tokens are JWT format
     * - Validated on each service call
     * - Auto-refresh if near expiry
     *
     * Validation Checks:
     * =================
     * 1. Token format valid
     * 2. Signature verified
     * 3. Not expired
     * 4. Not revoked
     * 5. User still active
     *
     * Request Format:
     * ==============
     * GET /ussd/validate-session?token=eyJhbGc...
     *
     * Response Format:
     * ===============
     * {
     *   "valid": true,
     *   "userId": 12345,
     *   "expiresIn": 300,
     *   "shouldRefresh": false
     * }
     *
     * @param token USSD session token to validate
     * @return Validation result with session details
     */
    @GetMapping("/ussd/validate-session")
    ResponseEntity<Map<String, Object>> validateUssdSession(
            @RequestParam("token") String token
    );
}