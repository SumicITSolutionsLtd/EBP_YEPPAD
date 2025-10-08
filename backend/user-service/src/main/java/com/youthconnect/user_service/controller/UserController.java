package com.youthconnect.user_service.controller;

import com.youthconnect.user_service.dto.request.ProfileUpdateRequest;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequestDTO;
import com.youthconnect.user_service.dto.response.ApiResponse;
import com.youthconnect.user_service.dto.response.MentorProfileDTO;
import com.youthconnect.user_service.dto.UserProfileDTO;
import com.youthconnect.user_service.entity.YouthProfile;
import com.youthconnect.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Profile Management Controller
 *
 * <p>This controller handles all user profile operations for authenticated users
 * in the Youth Connect Uganda platform. It serves as the primary interface for
 * profile management, mentor discovery, and USSD-based profile operations.</p>
 *
 * <h2>Architecture Overview:</h2>
 * <ul>
 *   <li>Authentication is validated by the API Gateway</li>
 *   <li>User context (JWT claims) is forwarded to this service</li>
 *   <li>Spring Security context holds the authenticated user information</li>
 * </ul>
 *
 * <h2>Key Responsibilities:</h2>
 * <ul>
 *   <li>Current user profile retrieval and updates</li>
 *   <li>USSD profile operations (phone-based lookup and updates)</li>
 *   <li>Mentor directory and search functionality</li>
 * </ul>
 *
 * <h2>Security Note:</h2>
 * All endpoints (except explicitly marked internal ones) require valid JWT authentication.
 * The API Gateway validates JWTs and forwards user context to this service.
 *
 * <h2>Exception Handling:</h2>
 * <p>This controller relies on exceptions defined in the exception package.
 * If UserNotFoundException is not yet created, services will throw standard
 * RuntimeException until proper exception hierarchy is implemented.</p>
 *
 * @author Youth Connect Uganda Development Team
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    // =========================================================================
    // CURRENT USER PROFILE OPERATIONS
    // =========================================================================

    /**
     * Retrieves the currently authenticated user's profile
     *
     * <p>This endpoint allows users to view their own profile information.
     * The user is identified from the Spring Security context which is
     * populated by the API Gateway after JWT validation.</p>
     *
     * <p><strong>Flow:</strong></p>
     * <ol>
     *   <li>Extract user email from Security Context</li>
     *   <li>Fetch profile from database using email</li>
     *   <li>Return complete profile information</li>
     * </ol>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>User viewing their dashboard</li>
     *   <li>Profile page rendering</li>
     *   <li>Settings page population</li>
     * </ul>
     *
     * @return ResponseEntity containing ApiResponse with UserProfileDTO
     * @throws RuntimeException if no authenticated user found
     */
    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Returns the complete profile of the currently authenticated user"
    )
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUserProfile() {
        // Extract email from Security Context
        String userEmail = getCurrentUserEmail();
        log.debug("Fetching profile for authenticated user: {}", maskEmail(userEmail));

        // Retrieve profile from service layer
        UserProfileDTO profile = userService.getUserProfileByEmail(userEmail);

        return ResponseEntity.ok(
                ApiResponse.success(profile, "Profile retrieved successfully")
        );
    }

    /**
     * Updates the currently authenticated user's profile
     *
     * <p>This endpoint allows users to update their profile information including
     * name, profession, description, and district. The method properly handles
     * the conversion from entity to DTO to ensure consistent API responses.</p>
     *
     * <p><strong>Key Fix:</strong> This version correctly converts the YouthProfile
     * entity returned by the service to a UserProfileDTO before returning to the client,
     * ensuring API response consistency.</p>
     *
     * <p><strong>Updatable Fields:</strong></p>
     * <ul>
     *   <li>First Name and Last Name</li>
     *   <li>Profession/Occupation</li>
     *   <li>Profile Description/Bio</li>
     *   <li>District/Location</li>
     * </ul>
     *
     * <p><strong>Validation:</strong></p>
     * Request body is validated using Jakarta Bean Validation annotations
     * defined in ProfileUpdateRequest DTO.
     *
     * @param request Profile update request containing new information
     * @return ResponseEntity containing ApiResponse with updated UserProfileDTO
     * @throws RuntimeException if no authenticated user found
     * @throws jakarta.validation.ValidationException if request validation fails
     */
    @PutMapping("/me/profile")
    @Operation(
            summary = "Update current user profile",
            description = "Updates the authenticated user's profile with new information"
    )
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateCurrentUserProfile(
            @Valid @RequestBody ProfileUpdateRequest request) {

        // Extract email from Security Context
        String userEmail = getCurrentUserEmail();
        log.info("Updating profile for user: {}", maskEmail(userEmail));

        // Call service method which returns YouthProfile entity
        YouthProfile youthProfile = userService.updateUserProfile(userEmail, request);

        // FIXED: Convert YouthProfile entity to UserProfileDTO for consistent API response
        // This ensures the response format matches the GET endpoint and API documentation
        UserProfileDTO profileDTO = UserProfileDTO.builder()
                .userId(youthProfile.getUser().getId())
                .firstName(youthProfile.getFirstName())
                .lastName(youthProfile.getLastName())
                .phoneNumber(youthProfile.getUser().getPhoneNumber())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(profileDTO, "Profile updated successfully")
        );
    }

    // =========================================================================
    // USSD PROFILE OPERATIONS
    // =========================================================================

    /**
     * Retrieves user profile by phone number
     *
     * <p>This endpoint is specifically designed for USSD service integration,
     * allowing profile lookups using phone numbers instead of email addresses.
     * This is crucial for feature phone users who access the platform via USSD.</p>
     *
     * <p><strong>USSD Context:</strong></p>
     * <ul>
     *   <li>USSD sessions are initiated by dialing a shortcode</li>
     *   <li>Phone number is the primary identifier</li>
     *   <li>No JWT authentication (handled differently by USSD gateway)</li>
     * </ul>
     *
     * <p><strong>Security Consideration:</strong></p>
     * This endpoint should be called only by the USSD service, which has
     * appropriate authentication at the gateway level.
     *
     * @param phoneNumber User's phone number (format: +256XXXXXXXXX)
     * @return ResponseEntity containing ApiResponse with UserProfileDTO
     * @throws RuntimeException if phone not found (or UserNotFoundException when implemented)
     */
    @GetMapping("/profile/phone/{phoneNumber}")
    @Operation(
            summary = "Get user profile by phone number",
            description = "Retrieves user profile using phone number - primarily for USSD service"
    )
    public ResponseEntity<ApiResponse<UserProfileDTO>> getUserProfileByPhone(
            @PathVariable String phoneNumber) {

        log.debug("Fetching profile for phone: {}", maskPhoneNumber(phoneNumber));

        // Retrieve profile using phone number as identifier
        UserProfileDTO profile = userService.getUserProfileByPhone(phoneNumber);

        return ResponseEntity.ok(
                ApiResponse.success(profile, "Profile retrieved successfully")
        );
    }

    /**
     * Updates user profile by phone number
     *
     * <p>This endpoint enables USSD-based profile updates. Users can update
     * their profiles through USSD menu interactions on feature phones without
     * internet access.</p>
     *
     * <p><strong>USSD Update Flow:</strong></p>
     * <ol>
     *   <li>User dials USSD code and navigates to profile update</li>
     *   <li>USSD service collects updated information via menu prompts</li>
     *   <li>USSD service calls this endpoint with phone number and updates</li>
     *   <li>Profile is updated and confirmation sent via USSD</li>
     * </ol>
     *
     * <p><strong>Note:</strong> Uses ProfileUpdateRequestDTO which may have
     * different fields than ProfileUpdateRequest to accommodate USSD limitations.</p>
     *
     * @param phoneNumber User's phone number (format: +256XXXXXXXXX)
     * @param request Profile update request from USSD service
     * @return ResponseEntity containing ApiResponse with updated UserProfileDTO
     * @throws RuntimeException if phone not found (or UserNotFoundException when implemented)
     * @throws jakarta.validation.ValidationException if request validation fails
     */
    @PutMapping("/profile/phone/{phoneNumber}")
    @Operation(
            summary = "Update user profile by phone number",
            description = "Updates user profile via phone number - primarily for USSD service"
    )
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateUserProfileByPhone(
            @PathVariable String phoneNumber,
            @Valid @RequestBody ProfileUpdateRequestDTO request) {

        log.info("Updating profile for phone: {}", maskPhoneNumber(phoneNumber));

        // Update profile using phone number as identifier
        UserProfileDTO updatedProfile = userService.updateUserProfileByPhone(
                phoneNumber, request
        );

        return ResponseEntity.ok(
                ApiResponse.success(updatedProfile, "Profile updated successfully")
        );
    }

    // =========================================================================
    // MENTOR DIRECTORY
    // =========================================================================

    /**
     * Retrieves all available mentors
     *
     * <p>This endpoint returns a comprehensive list of all registered mentors
     * in the Youth Connect platform. Mentors are users who have opted to provide
     * guidance and support to young people.</p>
     *
     * <p><strong>Mentor Information Includes:</strong></p>
     * <ul>
     *   <li>Basic profile information (name, bio)</li>
     *   <li>Areas of expertise</li>
     *   <li>Availability status</li>
     *   <li>Contact information</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Mentor discovery page</li>
     *   <li>Browse all mentors feature</li>
     *   <li>Mentor matching algorithms</li>
     * </ul>
     *
     * <p><strong>Note:</strong> Consider implementing pagination for
     * production use when mentor count grows.</p>
     *
     * @return ResponseEntity containing ApiResponse with List of MentorProfileDTO
     */
    @GetMapping("/mentors")
    @Operation(
            summary = "Get all mentors",
            description = "Returns a complete list of all available mentors in the platform"
    )
    public ResponseEntity<ApiResponse<List<MentorProfileDTO>>> getAllMentors() {
        log.debug("Fetching all mentors");

        // Retrieve all mentors from service layer
        List<MentorProfileDTO> mentors = userService.getAllMentors();

        return ResponseEntity.ok(
                ApiResponse.success(mentors, "Mentors retrieved successfully")
        );
    }

    /**
     * Retrieves a specific mentor by their ID
     *
     * <p>This endpoint fetches detailed information about a specific mentor,
     * typically used when a user wants to view a mentor's full profile before
     * deciding to connect or request mentorship.</p>
     *
     * <p><strong>Detailed Mentor Profile Includes:</strong></p>
     * <ul>
     *   <li>Complete biographical information</li>
     *   <li>Expertise areas and qualifications</li>
     *   <li>Mentorship history and reviews</li>
     *   <li>Availability and preferred communication channels</li>
     * </ul>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Mentor profile detail page</li>
     *   <li>Before initiating mentorship request</li>
     *   <li>Mentor verification and vetting</li>
     * </ul>
     *
     * <p><strong>Exception Handling:</strong></p>
     * If the mentor is not found, the service layer will throw an appropriate
     * exception (UserNotFoundException or RuntimeException until custom exceptions
     * are implemented).
     *
     * @param mentorId Unique identifier of the mentor
     * @return ResponseEntity containing ApiResponse with MentorProfileDTO
     * @throws RuntimeException if mentor not found (or custom exception when implemented)
     */
    @GetMapping("/mentors/{mentorId}")
    @Operation(
            summary = "Get mentor by ID",
            description = "Returns detailed profile information for a specific mentor"
    )
    public ResponseEntity<ApiResponse<MentorProfileDTO>> getMentorById(
            @PathVariable Long mentorId) {

        log.debug("Fetching mentor: {}", mentorId);

        // Retrieve specific mentor from service layer
        MentorProfileDTO mentor = userService.getMentorById(mentorId);

        return ResponseEntity.ok(
                ApiResponse.success(mentor, "Mentor retrieved successfully")
        );
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Extracts the current authenticated user's email from Spring Security context
     *
     * <p>In a production environment, this method would extract user information
     * from JWT claims that have been validated and forwarded by the API Gateway.
     * The gateway decodes the JWT and adds user information to the Security Context.</p>
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
     * @return Email address of the currently authenticated user
     * @throws RuntimeException if no authenticated user is found in context
     */
    private String getCurrentUserEmail() {
        // Retrieve authentication from Spring Security context
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        // Verify authentication exists and user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            // In production, this would extract email from JWT claims
            // Example: ((JwtAuthenticationToken) authentication).getToken().getClaim("email")
            // For now, return the principal name (typically username/email)
            return authentication.getName();
        }

        // No authenticated user found - this should never happen if security is configured correctly
        throw new RuntimeException("No authenticated user found in security context");
    }

    /**
     * Masks email address for logging purposes (GDPR/Privacy compliance)
     *
     * <p>This method partially obscures email addresses when logging to protect
     * user privacy while still maintaining useful debug information.</p>
     *
     * <p><strong>Example:</strong> john.doe@example.com → jo***@example.com</p>
     *
     * @param email Email address to mask
     * @return Masked email address for safe logging
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        // Show first 2 characters of local part, mask the rest
        String maskedLocal = localPart.substring(0, Math.min(2, localPart.length())) + "***";

        return maskedLocal + "@" + domain;
    }

    /**
     * Masks phone number for logging purposes (GDPR/Privacy compliance)
     *
     * <p>This method partially obscures phone numbers when logging to protect
     * user privacy while maintaining some identifiable information for debugging.</p>
     *
     * <p><strong>Example:</strong> +256701234567 → +25****567</p>
     *
     * @param phone Phone number to mask
     * @return Masked phone number for safe logging
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 6) {
            return "***";
        }

        // Show first 3 and last 3 digits, mask everything in between
        String prefix = phone.substring(0, 3);
        String suffix = phone.substring(phone.length() - 3);

        return prefix + "****" + suffix;
    }
}