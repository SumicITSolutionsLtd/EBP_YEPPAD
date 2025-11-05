package com.youthconnect.user_service.service;

import com.youthconnect.user_service.dto.UserProfileDTO;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequest;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequestDTO;
import com.youthconnect.user_service.dto.request.RegistrationRequest;
import com.youthconnect.user_service.dto.request.UssdRegistrationRequest;
import com.youthconnect.user_service.dto.response.MentorProfileDTO;
import com.youthconnect.user_service.controller.InternalUserController.UserProfileSummary;
import com.youthconnect.user_service.entity.Role;
import com.youthconnect.user_service.entity.User;
import com.youthconnect.user_service.entity.YouthProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * USER SERVICE INTERFACE - ENHANCED WITH INTERNAL API METHODS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * {@code UserService} defines the core business logic for managing users within
 * the Youth Connect Uganda platform. It handles user registration, profile
 * management, mentor interactions, account lifecycle operations, and provides
 * internal API methods for job-service integration.
 *
 * <p>Implementations of this interface must ensure transactional integrity and
 * validation of all user-related operations.</p>
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Enhanced with Internal API)
 * @since 2025-10-31
 */
public interface UserService {

    // ==============================================================
    // USER CREATION & REGISTRATION
    // ==============================================================

    /**
     * Creates a new {@link User} entity during registration.
     *
     * @param request the registration request containing user details
     * @return the persisted {@link User} entity
     */
    User createUser(RegistrationRequest request);

    /**
     * Creates a user profile after successful registration via web or app.
     *
     * @param user the associated {@link User} entity
     * @param request the registration request data
     * @return the created {@link UserProfileDTO}
     */
    UserProfileDTO createUserProfile(User user, RegistrationRequest request);

    /**
     * Creates a user profile for USSD-based registrations.
     *
     * @param user the associated {@link User} entity
     * @param request the USSD registration request
     * @return the created {@link UserProfileDTO}
     */
    UserProfileDTO createUssdUserProfile(User user, UssdRegistrationRequest request);

    // ==============================================================
    // USER RETRIEVAL
    // ==============================================================

    /**
     * Retrieves a user by email address.
     *
     * @param email the user's email
     * @return the {@link User} entity
     */
    User getUserByEmail(String email);

    /**
     * Retrieves a user by phone number.
     *
     * @param phoneNumber the user's phone number
     * @return the {@link User} entity
     */
    User getUserByPhone(String phoneNumber);

    /**
     * Retrieves a user by unique ID.
     *
     * @param userId the user's ID
     * @return the {@link User} entity
     */
    User getUserById(UUID userId);

    // ==============================================================
    // PROFILE RETRIEVAL
    // ==============================================================

    /**
     * Retrieves a full user profile by email.
     *
     * @param email the user's email
     * @return the {@link UserProfileDTO}
     */
    UserProfileDTO getUserProfileByEmail(String email);

    /**
     * Retrieves a full user profile by phone number.
     *
     * @param phoneNumber the user's phone number
     * @return the {@link UserProfileDTO}
     */
    UserProfileDTO getUserProfileByPhone(String phoneNumber);

    /**
     * Retrieves a full user profile by user ID.
     *
     * @param userId the user's ID
     * @return the {@link UserProfileDTO}
     */
    UserProfileDTO getUserProfileById(UUID userId);

    // ==============================================================
    // PROFILE UPDATES
    // ==============================================================

    /**
     * Updates a user's profile using their email.
     *
     * @param email the user's email
     * @param request the profile update request
     * @return the updated {@link YouthProfile} entity
     */
    YouthProfile updateUserProfile(String email, ProfileUpdateRequest request);

    /**
     * Updates a user's profile using their phone number.
     *
     * @param phoneNumber the user's phone number
     * @param request the profile update request DTO
     * @return the updated {@link UserProfileDTO}
     */
    UserProfileDTO updateUserProfileByPhone(String phoneNumber, ProfileUpdateRequestDTO request);

    // ==============================================================
    // MENTOR MANAGEMENT
    // ==============================================================

    /**
     * Retrieves all mentors with pagination.
     *
     * @param pageable pagination information
     * @return a page of {@link MentorProfileDTO}
     */
    Page<MentorProfileDTO> getAllMentors(Pageable pageable);

    /**
     * Retrieves a mentor's profile by UUID.
     *
     * @param mentorId the mentor's UUID
     * @return the {@link MentorProfileDTO}
     */
    MentorProfileDTO getMentorById(UUID mentorId);

    // ==============================================================
    // USER SEARCH & QUERY
    // ==============================================================

    /**
     * Retrieves all users assigned to a specific role.
     *
     * @param role the user {@link Role}
     * @return a list of {@link User} entities
     */
    List<User> getUsersByRole(Role role);

    /**
     * Searches users by a free-text term (e.g., name, email, or phone).
     *
     * @param searchTerm the search keyword
     * @return a list of matching {@link User} entities
     */
    List<User> searchUsers(String searchTerm);

    /**
     * Counts the number of users with a given role.
     *
     * @param role the user {@link Role}
     * @return the count of users
     */
    long getUserCountByRole(Role role);

    // Keep non-paginated for backward compatibility
    List<MentorProfileDTO> getAllMentors();

    // ==============================================================
    // VALIDATION HELPERS
    // ==============================================================

    /**
     * Checks whether an email is already registered.
     *
     * @param email the email to check
     * @return true if the email exists; false otherwise
     */
    boolean emailExists(String email);

    /**
     * Checks whether a phone number is already registered.
     *
     * @param phoneNumber the phone number to check
     * @return true if the phone exists; false otherwise
     */
    boolean phoneExists(String phoneNumber);

    // ==============================================================
    // ACCOUNT MANAGEMENT
    // ==============================================================

    /**
     * Deactivates a user account (soft delete).
     *
     * @param userId the user ID
     */
    void deactivateUser(UUID userId);

    /**
     * Reactivates a previously deactivated user account.
     *
     * @param userId the user ID
     */
    void reactivateUser(UUID userId);

    // ==============================================================
    // INTERNAL API METHODS (FOR JOB SERVICE INTEGRATION)
    // ==============================================================

    /**
     * Checks if a user exists by ID.
     *
     * <p>Used by job-service to verify user existence before creating
     * jobs or applications.</p>
     *
     * @param userId the user ID to check
     * @return true if user exists and is active; false otherwise
     */
    boolean userExists(UUID userId);

    /**
     * Retrieves a user profile summary for job service integration.
     *
     * <p>Returns essential user information needed for job posting
     * and application management without exposing sensitive data.</p>
     *
     * @param userId the user ID
     * @return {@link UserProfileSummary} with essential user information
     */
    UserProfileSummary getUserSummary(UUID userId);

    /**
     * Checks if a user has permission to post jobs.
     *
     * <p>Only users with roles NGO, COMPANY, RECRUITER, or GOVERNMENT
     * are allowed to create job postings.</p>
     *
     * @param userId the user ID to check
     * @return true if user can post jobs; false otherwise
     */
    boolean canUserPostJobs(UUID userId);

    /**
     * Retrieves the organization name for a user.
     *
     * <p>Used to display company/organization information on job postings.
     * Returns appropriate organization name based on user role.</p>
     *
     * @param userId the user ID
     * @return organization name or fallback identifier
     */
    String getUserOrganization(UUID userId);
}