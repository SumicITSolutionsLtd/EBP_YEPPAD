package com.youthconnect.job_services.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Gateway User Context Utility - Complete Implementation
 *
 * ============================================================================
 * PURPOSE
 * ============================================================================
 * Extracts authenticated user information from API Gateway headers.
 * This replaces the previous JWT-based authentication in the service.
 *
 * ============================================================================
 * AUTHENTICATION FLOW
 * ============================================================================
 *
 * Step 1: User sends request with JWT token to API Gateway
 *   Example: GET /api/v1/jobs/my-jobs
 *   Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
 *
 * Step 2: API Gateway validates JWT token
 *   - Verifies signature with secret key
 *   - Checks expiration timestamp
 *   - Validates issuer and audience
 *   - Extracts user claims (userId, role, email)
 *
 * Step 3: Gateway adds user context headers
 *   - X-User-Id: 550e8400-e29b-41d4-a716-446655440000 (UUID)
 *   - X-User-Role: COMPANY (enum: YOUTH, NGO, COMPANY, etc.)
 *   - X-Auth-Token: eyJhbGciOiJIUzI1NiIs... (original JWT, optional)
 *
 * Step 4: Gateway forwards request to Job Service
 *   GET http://job-service:8000/api/v1/jobs/my-jobs
 *   Headers:
 *     X-User-Id: 550e8400-e29b-41d4-a716-446655440000
 *     X-User-Role: COMPANY
 *     X-Auth-Token: eyJhbGciOiJIUzI1NiIs...
 *
 * Step 5: This utility extracts user context from headers
 *   UUID userId = GatewayUserContextUtil.getCurrentUserId();
 *   String role = GatewayUserContextUtil.getCurrentUserRole();
 *
 * Step 6: Service performs business logic with user context
 *   - Authorization checks (can user perform this action?)
 *   - Data filtering (show only user's jobs)
 *   - Audit logging (who performed this action?)
 *
 * ============================================================================
 * SECURITY MODEL
 * ============================================================================
 *
 * ⚠️ CRITICAL SECURITY ASSUMPTIONS:
 *
 * 1. TRUSTED NETWORK:
 *    - This service MUST run in a private network
 *    - ONLY the API Gateway should have direct access
 *    - Use VPC, security groups, or firewall rules
 *    - Direct public access = CRITICAL SECURITY VULNERABILITY
 *
 * 2. HEADER TRUST:
 *    - This service trusts X-User-Id and X-User-Role headers completely
 *    - If an attacker can add these headers, they can impersonate any user
 *    - Network-level security MUST prevent header spoofing
 *
 * 3. PRODUCTION ENHANCEMENTS (TODO):
 *    - Add header signature validation (HMAC or RSA)
 *    - Implement shared secret between gateway and services
 *    - Add request timestamps and nonces to prevent replay attacks
 *    - Example: X-Gateway-Signature: HMAC-SHA256(headers + timestamp + secret)
 *
 * ============================================================================
 * USAGE EXAMPLES
 * ============================================================================
 *
 * Example 1: Get Current User ID
 * -------------------------------
 * @GetMapping("/my-jobs")
 * public ResponseEntity<List<Job>> getMyJobs() {
 *     UUID userId = GatewayUserContextUtil.getCurrentUserId();
 *     List<Job> jobs = jobService.getJobsByPoster(userId);
 *     return ResponseEntity.ok(jobs);
 * }
 *
 * Example 2: Check User Role
 * ---------------------------
 * @PostMapping("/jobs")
 * public ResponseEntity<Job> createJob(@RequestBody CreateJobRequest request) {
 *     // Check if user has permission to post jobs
 *     if (!GatewayUserContextUtil.hasAnyRole("NGO", "COMPANY", "GOVERNMENT")) {
 *         throw new UnauthorizedAccessException("Only organizations can post jobs");
 *     }
 *
 *     UUID userId = GatewayUserContextUtil.getCurrentUserId();
 *     String role = GatewayUserContextUtil.getCurrentUserRole();
 *
 *     Job job = jobService.createJob(request, userId, role);
 *     return ResponseEntity.ok(job);
 * }
 *
 * Example 3: Require Authentication
 * ----------------------------------
 * @GetMapping("/applications")
 * public ResponseEntity<List<Application>> getMyApplications() {
 *     // Ensure user is authenticated (throw exception if not)
 *     GatewayUserContextUtil.requireUserContext();
 *
 *     UUID userId = GatewayUserContextUtil.getCurrentUserId();
 *     List<Application> applications = applicationService.getMyApplications(userId);
 *     return ResponseEntity.ok(applications);
 * }
 *
 * ============================================================================
 * ERROR HANDLING
 * ============================================================================
 * - If headers are missing, methods return null (graceful degradation)
 * - Use requireUserContext() to enforce authentication
 * - Invalid UUID format throws IllegalStateException
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 3.0.0
 */
@Slf4j
@Component
public class GatewayUserContextUtil {

    // ============================================================================
    // HEADER NAMES (Must match API Gateway configuration)
    // ============================================================================

    /**
     * Header containing user's UUID
     * Added by API Gateway after JWT validation
     * Example: X-User-Id: 550e8400-e29b-41d4-a716-446655440000
     */
    private static final String HEADER_USER_ID = "X-User-Id";

    /**
     * Header containing user's role
     * Added by API Gateway after JWT validation
     * Example: X-User-Role: COMPANY
     * Possible values: YOUTH, NGO, COMPANY, RECRUITER, GOVERNMENT, ADMIN
     */
    private static final String HEADER_USER_ROLE = "X-User-Role";

    /**
     * Header containing original JWT token (optional)
     * Useful for audit logs, forwarding to other services
     * Example: X-Auth-Token: eyJhbGciOiJIUzI1NiIs...
     */
    private static final String HEADER_AUTH_TOKEN = "X-Auth-Token";

    // ============================================================================
    // PRIVATE HELPER METHODS
    // ============================================================================

    /**
     * Get current HTTP request from Spring context
     *
     * Uses Spring's RequestContextHolder to access the current HTTP request.
     * This works because Spring stores request information in a ThreadLocal
     * variable during request processing.
     *
     * Returns null if:
     * - Called outside of HTTP request thread (e.g., scheduled task)
     * - Called before request context is initialized
     * - Called in async thread without context propagation
     *
     * @return HttpServletRequest or null if not in request context
     */
    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            log.warn("No request context found - not in HTTP request thread. " +
                    "This is normal for scheduled tasks, async methods, or initialization code.");
            return null;
        }

        return attributes.getRequest();
    }

    // ============================================================================
    // PUBLIC API - USER CONTEXT EXTRACTION
    // ============================================================================

    /**
     * Extract user ID from gateway headers
     *
     * Reads the X-User-Id header added by the API Gateway after JWT validation.
     * The header contains the user's UUID as a string, which is parsed and returned.
     *
     * RETURN VALUES:
     * - Valid UUID: User is authenticated, UUID successfully parsed
     * - null: Header not present (unauthenticated request or public endpoint)
     *
     * EXCEPTIONS:
     * - IllegalStateException: Header present but contains invalid UUID format
     *
     * @return User UUID or null if not present
     * @throws IllegalStateException if header contains invalid UUID format
     */
    public static UUID getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String userIdHeader = request.getHeader(HEADER_USER_ID);
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            log.debug("No {} header found in request. " +
                    "This is normal for public endpoints.", HEADER_USER_ID);
            return null;
        }

        try {
            UUID userId = UUID.fromString(userIdHeader);
            log.debug("Extracted userId from header: {}", userId);
            return userId;
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in {} header: {}. " +
                            "This should never happen if API Gateway is configured correctly.",
                    HEADER_USER_ID, userIdHeader);
            throw new IllegalStateException(
                    "Invalid user ID format in request headers. Please contact support.",
                    e
            );
        }
    }

    /**
     * Extract user role from gateway headers
     *
     * Reads the X-User-Role header added by the API Gateway after JWT validation.
     * The header contains the user's role as a string (e.g., "COMPANY", "NGO").
     *
     * POSSIBLE ROLE VALUES:
     * - YOUTH: Young person looking for jobs/opportunities
     * - NGO: Non-governmental organization posting jobs
     * - COMPANY: Private company posting jobs
     * - RECRUITER: Professional recruiter posting jobs
     * - GOVERNMENT: Government entity posting jobs
     * - ADMIN: Platform administrator (full access)
     *
     * @return User role or null if not present
     */
    public static String getCurrentUserRole() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String role = request.getHeader(HEADER_USER_ROLE);
        if (role == null || role.trim().isEmpty()) {
            log.debug("No {} header found in request. " +
                    "This is normal for public endpoints.", HEADER_USER_ROLE);
            return null;
        }

        log.debug("Extracted user role from header: {}", role);
        return role.trim();
    }

    /**
     * Extract original JWT token from gateway headers
     *
     * Reads the X-Auth-Token header containing the original JWT token that was
     * validated by the API Gateway. This header is optional and mainly used for:
     * - Audit logging (track which token was used)
     * - Forwarding to other services that require the original token
     * - Debugging and troubleshooting authentication issues
     *
     * ⚠️ NOTE: This service does NOT validate the token - the gateway already did that.
     * We trust the gateway's validation and only use this for logging/forwarding.
     *
     * @return Original JWT token or null if not present
     */
    public static String getOriginalAuthToken() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        String token = request.getHeader(HEADER_AUTH_TOKEN);
        if (token != null && !token.trim().isEmpty()) {
            log.debug("Original auth token found in headers (not shown for security reasons)");
            return token.trim();
        }

        return null;
    }

    // ============================================================================
    // PUBLIC API - ROLE CHECKING
    // ============================================================================

    /**
     * Check if current user has a specific role
     *
     * Performs case-insensitive role comparison. Useful for authorization checks
     * before performing sensitive operations.
     *
     * @param requiredRole Role to check (e.g., "COMPANY", "NGO")
     * @return true if user has the role, false otherwise
     */
    public static boolean hasRole(String requiredRole) {
        String currentRole = getCurrentUserRole();
        if (currentRole == null || requiredRole == null) {
            return false;
        }

        boolean hasRole = currentRole.equalsIgnoreCase(requiredRole);
        log.debug("User role check: current={}, required={}, result={}",
                currentRole, requiredRole, hasRole);
        return hasRole;
    }

    /**
     * Check if current user has any of the specified roles
     *
     * Performs case-insensitive role comparison against multiple roles.
     * Returns true if user has ANY of the specified roles.
     *
     * @param allowedRoles Roles to check against (varargs)
     * @return true if user has any of the roles, false otherwise
     */
    public static boolean hasAnyRole(String... allowedRoles) {
        String currentRole = getCurrentUserRole();
        if (currentRole == null || allowedRoles == null || allowedRoles.length == 0) {
            return false;
        }

        for (String allowedRole : allowedRoles) {
            if (currentRole.equalsIgnoreCase(allowedRole)) {
                log.debug("User has allowed role: {}", allowedRole);
                return true;
            }
        }

        log.debug("User role {} does not match any of: {}",
                currentRole, String.join(", ", allowedRoles));
        return false;
    }

    // ============================================================================
    // PUBLIC API - AUTHENTICATION VERIFICATION
    // ============================================================================

    /**
     * Verify that user context is available
     *
     * Throws exception if user is not authenticated (no X-User-Id header).
     * Use this method to enforce authentication for protected endpoints.
     *
     * This is more explicit than checking if getCurrentUserId() returns null,
     * and provides a clear error message for unauthenticated requests.
     *
     * @throws IllegalStateException if user context is not available
     */
    public static void requireUserContext() {
        UUID userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException(
                    "User context not found. This endpoint requires authentication via API Gateway. " +
                            "Please ensure your request includes a valid JWT token in the Authorization header."
            );
        }
    }

    /**
     * Check if user is authenticated
     *
     * Returns true if X-User-Id header is present and valid.
     * This is a non-throwing alternative to requireUserContext().
     *
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }

    // ============================================================================
    // PUBLIC API - DEBUGGING & LOGGING
    // ============================================================================

    /**
     * Get current user context as a formatted string
     *
     * Useful for logging and debugging. Returns a human-readable string
     * containing the user's ID and role.
     *
     * Examples:
     * - "User[id=550e8400-e29b-41d4-a716-446655440000, role=COMPANY]"
     * - "Anonymous User (No Context)"
     *
     * @return Formatted user context string
     */
    public static String getUserContextString() {
        UUID userId = getCurrentUserId();
        String role = getCurrentUserRole();

        if (userId == null && role == null) {
            return "Anonymous User (No Context)";
        }

        return String.format("User[id=%s, role=%s]",
                userId != null ? userId : "unknown",
                role != null ? role : "unknown");
    }

    /**
     * Log current user context (for debugging)
     *
     * Logs user ID and role at DEBUG level.
     * Useful for troubleshooting authentication issues.
     */
    public static void logUserContext() {
        UUID userId = getCurrentUserId();
        String role = getCurrentUserRole();

        log.debug("Current user context: userId={}, role={}", userId, role);
    }
}