package com.youthconnect.user_service.controller;

import com.youthconnect.user_service.dto.request.ProfileUpdateRequest;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequestDTO;
import com.youthconnect.user_service.dto.response.ApiResponse;
import com.youthconnect.user_service.dto.response.MentorProfileDTO;
import com.youthconnect.user_service.dto.response.UserProfileResponse;
import com.youthconnect.user_service.dto.UserProfileDTO;
import com.youthconnect.user_service.entity.YouthProfile;
import com.youthconnect.user_service.service.UserService;
import com.youthconnect.user_service.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID; // Changed Long to UUID for mentorId based on previous context, adjust if Mentor ID is Long

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * UNIFIED USER CONTROLLER - PRODUCTION-READY IMPLEMENTATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Comprehensive user profile management controller for Kwetu-Hub Uganda.
 * Handles all user operations including:
 * - Current user profile operations (view, update)
 * - Enhanced profiles with job statistics
 * - USSD-based profile operations (phone lookup)
 * - Mentor directory and discovery
 * - Job application statistics
 *
 * <h2>Architecture Overview:</h2>
 * <ul>
 *   <li>Authentication validated by API Gateway (JWT)</li>
 *   <li>User context forwarded to this service via Security Context</li>
 *   <li>Integrates with job-service via Feign for statistics</li>
 * </ul>
 *
 * <h2>Security:</h2>
 * All endpoints require valid JWT authentication except internal/USSD endpoints.
 *
 * @author Douglas Kings Kato & Youth Connect Uganda Team
 * @version 3.2.0 (Merged & Refined)
 * @since 2025-10-31
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Comprehensive user profile management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;
    private final UserServiceImpl userServiceImpl; // Assuming UserServiceImpl has methods like getEnhancedUserProfileByEmail and getUserById

    // =========================================================================
    // ENHANCED PROFILE OPERATIONS (WITH JOB STATISTICS)
    // =========================================================================

    /**
     * GET /api/v1/users/profile/enhanced
     *
     * Retrieves comprehensive user profile including:
     * - Basic user information (name, email, phone, role)
     * - Profile details (profession, district, bio)
     * - Job application statistics (total, pending, approved, rejected)
     * - Success rate calculation
     * - Current employment status and details
     *
     * This endpoint integrates with job-service via Feign client to fetch
     * real-time job statistics. Uses circuit breaker pattern for resilience.
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>User dashboard with comprehensive stats</li>
     *   <li>Profile pages with job history</li>
     *   <li>Mobile app profile screens</li>
     *   <li>Admin user overview</li>
     * </ul>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * {
     *   "userId": 123,
     *   "email": "john@example.com",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "role": "YOUTH",
     *   "totalJobApplications": 15,
     *   "pendingJobApplications": 3,
     *   "approvedJobApplications": 8,
     *   "rejectedJobApplications": 4,
     *   "jobApplicationSuccessRate": 53.33,
     *   "currentEmploymentStatus": "Employed at Tech Solutions Ltd",
     *   "isActive": true,
     *   "emailVerified": true,
     *   "phoneVerified": true
     * }
     * }</pre>
     *
     * @param userDetails Authenticated user details from JWT token
     * @return ResponseEntity with UserProfileResponse containing all profile data
     * @throws RuntimeException if no authenticated user found
     */
    @GetMapping("/profile/enhanced")
    @Operation(
            summary = "Get enhanced user profile with job statistics",
            description = "Returns comprehensive profile including job application stats, " +
                    "employment status, and success rate metrics"
    )
    public ResponseEntity<UserProfileResponse> getEnhancedProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("📊 Fetching enhanced profile for user: {}", maskEmail(userDetails.getUsername()));

        try {
            // Fetch comprehensive profile with job statistics
            UserProfileResponse profile = userServiceImpl.getEnhancedUserProfileByEmail(
                    userDetails.getUsername()
            );

            log.info("✅ Enhanced profile retrieved successfully for user: {}",
                    maskEmail(userDetails.getUsername()));

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            log.error("❌ Failed to fetch enhanced profile for user {}: {}",
                    maskEmail(userDetails.getUsername()), e.getMessage());
            throw e;
        }
    }

    /**
     * GET /api/v1/users/{userId}/profile/enhanced
     *
     * Admin/Internal endpoint to fetch any user's enhanced profile by ID.
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Admin dashboards</li>
     *   <li>Internal service-to-service calls</li>
     *   <li>Reporting systems</li>
     *   <li>User analytics</li>
     * </ul>
     *
     * @param userId User ID to fetch
     * @return ResponseEntity with UserProfileResponse
     * @throws RuntimeException if user not found
     */
    @GetMapping("/{userId}/profile/enhanced")
    @Operation(
            summary = "Get enhanced profile by user ID (Admin)",
            description = "Admin endpoint to retrieve any user's comprehensive profile with job statistics"
    )
    public ResponseEntity<UserProfileResponse> getEnhancedProfileById(
            @PathVariable UUID userId) { // Changed Long to UUID based on YouthProfile's user ID type

        log.info("📊 Fetching enhanced profile for userId: {}", userId);

        // First get basic user info
        var user = userServiceImpl.getUserById(userId); // Assuming this method returns a user object with an email

        // Then get enhanced profile with job stats
        UserProfileResponse profile = userServiceImpl.getEnhancedUserProfileByEmail(
                user.getEmail()
        );

        log.info("✅ Enhanced profile retrieved for userId: {}", userId);

        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/v1/users/stats/job-applications
     *
     * Quick endpoint to fetch ONLY job application statistics without full profile.
     *
     * <p><strong>FIXED VERSION:</strong> Resolves type inference issues by using HashMap
     * with explicit Object type casting and proper null handling.</p>
     *
     * <p><strong>Useful for:</strong></p>
     * <ul>
     *   <li>Dashboard widgets that only need stats</li>
     *   <li>Mobile apps with limited bandwidth</li>
     *   <li>Quick status checks</li>
     *   <li>Real-time stat updates</li>
     * </ul>
     *
     * <p><strong>Response Example:</strong></p>
     * <pre>{@code
     * {
     *   "totalApplications": 15,
     *   "pendingApplications": 3,
     *   "approvedApplications": 8,
     *   "rejectedApplications": 4,
     *   "successRate": 53.33,
     *   "currentEmploymentStatus": "Employed at Tech Solutions Ltd",
     *   "isEmployed": true
     * }
     * }</pre>
     *
     * @param userDetails Authenticated user details
     * @return ResponseEntity with job statistics map
     */
    @GetMapping("/stats/job-applications")
    @Operation(
            summary = "Get job application statistics only",
            description = "Returns only job-related statistics without full profile data - " +
                    "optimized for quick stat checks and dashboard widgets"
    )
    public ResponseEntity<Map<String, Object>> getJobApplicationStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("📈 Fetching job stats for user: {}", maskEmail(userDetails.getUsername()));

        try {
            // Fetch user and enhanced profile
            var user = userService.getUserByEmail(userDetails.getUsername()); // Assuming this method exists
            UserProfileResponse profile = userServiceImpl.getEnhancedUserProfileByEmail(
                    user.getEmail()
            );

            // FIXED: Use HashMap with explicit Object type for proper type inference
            // This resolves compilation issues with Map.of() when dealing with mixed types
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalApplications",
                    profile.getTotalJobApplications() != null
                            ? profile.getTotalJobApplications()
                            : Integer.valueOf(0));
            stats.put("pendingApplications",
                    profile.getPendingJobApplications() != null
                            ? profile.getPendingJobApplications()
                            : Integer.valueOf(0));
            stats.put("approvedApplications",
                    profile.getApprovedJobApplications() != null
                            ? profile.getApprovedJobApplications()
                            : Integer.valueOf(0));
            stats.put("rejectedApplications",
                    profile.getRejectedJobApplications() != null
                            ? profile.getRejectedJobApplications()
                            : Integer.valueOf(0));
            stats.put("successRate",
                    profile.getJobApplicationSuccessRate() != null
                            ? profile.getJobApplicationSuccessRate()
                            : Double.valueOf(0.0));
            stats.put("currentEmploymentStatus",
                    profile.getCurrentEmploymentStatus() != null
                            ? profile.getCurrentEmploymentStatus()
                            : "Seeking Opportunities");
            stats.put("isEmployed", Boolean.valueOf(profile.isEmployed()));

            log.info("✅ Job stats retrieved successfully for user: {}",
                    maskEmail(userDetails.getUsername()));

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("❌ Failed to fetch job stats for user {}: {}",
                    maskEmail(userDetails.getUsername()), e.getMessage());
            throw e;
        }
    }

    // =========================================================================
    // CURRENT USER PROFILE OPERATIONS (BASIC)
    // =========================================================================

    /**
     * GET /api/v1/users/me
     *
     * Retrieves the currently authenticated user's basic profile.
     * This is the standard endpoint for profile viewing without job statistics.
     *
     * <p><strong>Flow:</strong></p>
     * <ol>
     *   <li>Extract user email from Security Context</li>
     *   <li>Fetch profile from database</li>
     *   <li>Return basic profile information</li>
     * </ol>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Simple profile viewing</li>
     *   <li>Settings page population</li>
     *   <li>Quick profile checks</li>
     * </ul>
     *
     * @return ResponseEntity containing ApiResponse with UserProfileDTO
     * @throws RuntimeException if no authenticated user found
     */
    @GetMapping("/me")
    @Operation(
            summary = "Get current user's basic profile",
            description = "Returns basic profile of the currently authenticated user"
    )
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUserProfile() {
        String userEmail = getCurrentUserEmail();
        log.debug("📄 Fetching basic profile for: {}", maskEmail(userEmail));

        UserProfileDTO profile = userService.getUserProfileByEmail(userEmail);

        return ResponseEntity.ok(
                ApiResponse.success(profile, "Profile retrieved successfully")
        );
    }

    /**
     * GET /api/v1/users/profile (Legacy endpoint)
     *
     * Alternative endpoint for basic profile retrieval.
     * Maintained for backward compatibility.
     *
     * @param userDetails Authenticated user details from JWT token
     * @return ResponseEntity with basic UserProfileDTO
     */
    @GetMapping("/profile")
    @Operation(
            summary = "Get basic user profile (Legacy)",
            description = "Legacy endpoint for basic profile retrieval"
    )
    public ResponseEntity<ApiResponse<UserProfileDTO>> getBasicProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("📄 Fetching basic profile for user: {}", maskEmail(userDetails.getUsername()));

        var profile = userService.getUserProfileByEmail(userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success(profile, "Profile retrieved successfully")
        );
    }

    /**
     * PUT /api/v1/users/me/profile
     *
     * Updates the currently authenticated user's profile.
     *
     * <p><strong>IMPORTANT FIX:</strong> This version correctly converts the YouthProfile
     * entity returned by the service to a UserProfileDTO before returning, ensuring
     * consistent API response format.</p>
     *
     * <p><strong>Updatable Fields:</strong></p>
     * <ul>
     *   <li>First Name and Last Name</li>
     *   <li>Profession/Occupation</li>
     *   <li>Profile Description/Bio</li>
     *   <li>District/Location</li>
     * </ul>
     *
     * @param request Profile update request containing new information
     * @return ResponseEntity containing ApiResponse with updated UserProfileDTO
     * @throws RuntimeException if no authenticated user found
     * @throws jakarta.validation.ValidationException if request validation fails
     */
    @PutMapping("/me/profile")
    @Operation(
            summary = "Update current user's profile",
            description = "Updates the authenticated user's profile with new information"
    )
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateCurrentUserProfile(
            @Valid @RequestBody ProfileUpdateRequest request) {

        String userEmail = getCurrentUserEmail();
        log.info("✏️ Updating profile for user: {}", maskEmail(userEmail));

        // Call service method which returns YouthProfile entity
        YouthProfile youthProfile = userService.updateUserProfile(userEmail, request);

        // CRITICAL FIX: Convert YouthProfile entity to UserProfileDTO
        // This ensures consistent API response format
        UserProfileDTO profileDTO = UserProfileDTO.builder()
                .userId(youthProfile.getUser().getId())
                .firstName(youthProfile.getFirstName())
                .lastName(youthProfile.getLastName())
                .phoneNumber(youthProfile.getUser().getPhoneNumber())
                .email(youthProfile.getUser().getEmail()) // Added email for completeness
                .profession(youthProfile.getProfession())
                .district(youthProfile.getDistrict())
                .description(youthProfile.getDescription())
                .build();

        log.info("✅ Profile updated successfully for user: {}", maskEmail(userEmail));

        return ResponseEntity.ok(
                ApiResponse.success(profileDTO, "Profile updated successfully")
        );
    }

    // =========================================================================
    // USSD PROFILE OPERATIONS (PHONE-BASED ACCESS)
    // =========================================================================

    /**
     * GET /api/v1/users/profile/phone/{phoneNumber}
     *
     * Retrieves user profile by phone number.
     * Designed specifically for USSD service integration.
     *
     * <p><strong>USSD Context:</strong></p>
     * <ul>
     *   <li>USSD sessions initiated by dialing shortcode (*256#)</li>
     *   <li>Phone number is primary identifier</li>
     *   <li>No JWT authentication (handled by USSD gateway)</li>
     *   <li>Enables feature phone access</li>
     * </ul>
     *
     * <p><strong>Security Note:</strong></p>
     * This endpoint should only be called by the USSD service, which has
     * appropriate authentication at the gateway level.
     *
     * @param phoneNumber User's phone number (format: +256XXXXXXXXX)
     * @return ResponseEntity containing ApiResponse with UserProfileDTO
     * @throws RuntimeException if phone not found
     */
    @GetMapping("/profile/phone/{phoneNumber}")
    @Operation(
            summary = "Get user profile by phone number (USSD)",
            description = "Retrieves user profile using phone number - primarily for USSD service integration"
    )
    public ResponseEntity<ApiResponse<UserProfileDTO>> getUserProfileByPhone(
            @PathVariable String phoneNumber) {

        log.debug("📞 Fetching profile for phone: {}", maskPhoneNumber(phoneNumber));

        UserProfileDTO profile = userService.getUserProfileByPhone(phoneNumber);

        return ResponseEntity.ok(
                ApiResponse.success(profile, "Profile retrieved successfully")
        );
    }

    /**
     * PUT /api/v1/users/profile/phone/{phoneNumber}
     *
     * Updates user profile by phone number.
     * Enables USSD-based profile updates for feature phone users.
     *
     * <p><strong>USSD Update Flow:</strong></p>
     * <ol>
     *   <li>User dials USSD code and navigates to profile update</li>
     *   <li>USSD service collects updated information via menu prompts</li>
     *   <li>USSD service calls this endpoint with phone and updates</li>
     *   <li>Profile updated and confirmation sent via USSD</li>
     * </ol>
     *
     * @param phoneNumber User's phone number (format: +256XXXXXXXXX)
     * @param request Profile update request from USSD service
     * @return ResponseEntity containing ApiResponse with updated UserProfileDTO
     * @throws RuntimeException if phone not found
     * @throws jakarta.validation.ValidationException if validation fails
     */
    @PutMapping("/profile/phone/{phoneNumber}")
    @Operation(
            summary = "Update user profile by phone number (USSD)",
            description = "Updates user profile via phone number - primarily for USSD service integration"
    )
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateUserProfileByPhone(
            @PathVariable String phoneNumber,
            @Valid @RequestBody ProfileUpdateRequestDTO request) {

        log.info("✏️ Updating profile for phone: {}", maskPhoneNumber(phoneNumber));

        UserProfileDTO updatedProfile = userService.updateUserProfileByPhone(
                phoneNumber, request
        );

        log.info("✅ Profile updated successfully for phone: {}", maskPhoneNumber(phoneNumber));

        return ResponseEntity.ok(
                ApiResponse.success(updatedProfile, "Profile updated successfully")
        );
    }

    // =========================================================================
    // MENTOR DIRECTORY
    // =========================================================================

    /**
     * UPDATED: Get all mentors with pagination, sorting, and search.
     * Returns paginated list of available mentors.
     *
     * <p><strong>Improvements:</strong></p>
     * <ul>
     *   <li>Added pagination (page, size)</li>
     *   <li>Added sorting (sortBy, sortDirection)</li>
     *   <li>Consolidated mentor listing logic</li>
     * </ul>
     *
     * @param page Current page number (0-indexed)
     * @param size Number of items per page
     * @param sortBy Field to sort by (e.g., "createdAt", "lastName")
     * @param sortDirection Sort order (ASC or DESC)
     * @return ResponseEntity containing Page of MentorProfileDTO
     */
    @GetMapping("/mentors")
    @Operation(
            summary = "Get all mentors (paginated)",
            description = "Returns paginated list of all available mentors with sorting options"
    )
    public ResponseEntity<Page<MentorProfileDTO>> getAllMentors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.debug("👨‍🏫 Fetching mentors - page: {}, size: {}, sortBy: {}, sortDirection: {}", page, size, sortBy, sortDirection);

        Pageable pageable = PageRequest.of(
                page,
                size,
                sortDirection.equalsIgnoreCase("DESC")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending()
        );

        Page<MentorProfileDTO> mentors = userService.getAllMentors(pageable); // Assuming an overloaded method in UserService

        log.info("✅ Retrieved {} mentors (page {}/{})",
                mentors.getNumberOfElements(),
                mentors.getNumber() + 1,
                mentors.getTotalPages()
        );

        return ResponseEntity.ok(mentors);
    }

    /**
     * GET /api/v1/users/mentors/{mentorId}
     *
     * Retrieves detailed information about a specific mentor.
     *
     * <p><strong>Detailed Mentor Profile Includes:</strong></p>
     * <ul>
     *   <li>Complete biographical information</li>
     *   <li>Expertise areas and qualifications</li>
     *   <li>Mentorship history and success stories</li>
     *   <li>Reviews and ratings from mentees</li>
     *   <li>Availability and preferred communication channels</li>
     *   <li>Specializations and industries</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Mentor profile detail page</li>
     *   <li>Before initiating mentorship request</li>
     *   <li>Mentor verification and vetting</li>
     *   <li>Matching algorithm input</li>
     * </ul>
     *
     * @param mentorId Unique identifier of the mentor
     * @return ResponseEntity containing ApiResponse with MentorProfileDTO
     * @throws RuntimeException if mentor not found
     */
    @GetMapping("/mentors/{mentorId}")
    @Operation(
            summary = "Get mentor by ID",
            description = "Returns detailed profile information for a specific mentor"
    )
    public ResponseEntity<ApiResponse<MentorProfileDTO>> getMentorById(
            @PathVariable UUID mentorId) { // Changed Long to UUID consistent with the original
        // provided `getAllMentors` where it returned Page<MentorProfileDTO>

        log.debug("👨‍🏫 Fetching mentor: {}", mentorId);

        MentorProfileDTO mentor = userService.getMentorById(mentorId);

        log.info("✅ Mentor retrieved: {}", mentorId);

        return ResponseEntity.ok(
                ApiResponse.success(mentor, "Mentor retrieved successfully")
        );
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Extracts the current authenticated user's email from Spring Security context.
     *
     * <p><strong>Security Flow:</strong></p>
     * <ol>
     *   <li>Client sends JWT in Authorization header</li>
     *   <li>API Gateway validates JWT signature and expiration</li>
     *   <li>Gateway extracts claims and creates Authentication object</li>
     *   <li>Authentication forwarded to this service</li>
     *   <li>This method retrieves user email from Authentication</li>
     * </ol>
     *
     * @return Email address of currently authenticated user
     * @throws RuntimeException if no authenticated user in context
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // In a real microservice with JWT, you might extract this from a JwtAuthenticationToken
            // For a basic setup, authentication.getName() usually returns the principal's username (often email)
            return authentication.getName();
        }

        throw new RuntimeException("No authenticated user found in security context");
    }

    /**
     * Masks email address for logging (GDPR/Privacy compliance).
     *
     * <p><strong>Example:</strong> john.doe@example.com → jo***@example.com</p>
     *
     * @param email Email address to mask
     * @return Masked email for safe logging
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        String maskedLocal = localPart.substring(0, Math.min(2, localPart.length())) + "***";

        return maskedLocal + "@" + domain;
    }

    /**
     * Masks phone number for logging (GDPR/Privacy compliance).
     *
     * <p><strong>Example:</strong> +256701234567 → +25****567</p>
     *
     * @param phone Phone number to mask
     * @return Masked phone for safe logging
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 6) {
            return "***";
        }

        String prefix = phone.substring(0, 3);
        String suffix = phone.substring(phone.length() - 3);

        return prefix + "****" + suffix;
    }
}