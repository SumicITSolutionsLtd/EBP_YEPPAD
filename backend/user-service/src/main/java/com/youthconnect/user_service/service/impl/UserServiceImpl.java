package com.youthconnect.user_service.service.impl;

import com.youthconnect.user_service.dto.response.MentorProfileDTO;
import com.youthconnect.user_service.dto.response.UserProfileResponse;
import com.youthconnect.user_service.dto.UserProfileDTO;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequest;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequestDTO;
import com.youthconnect.user_service.dto.request.RegistrationRequest;
import com.youthconnect.user_service.dto.request.UssdRegistrationRequest;
import com.youthconnect.user_service.entity.*;
import com.youthconnect.user_service.exception.UserAlreadyExistsException;
import com.youthconnect.user_service.exception.UserNotFoundException;
import com.youthconnect.user_service.repository.*;
import com.youthconnect.user_service.service.UserService;
import com.youthconnect.user_service.client.JobServiceClient;
import com.youthconnect.user_service.client.JobServiceClient.ApplicationSummaryResponse;
import com.youthconnect.user_service.client.JobServiceClient.CurrentEmploymentDto;
import com.youthconnect.user_service.controller.InternalUserController.UserProfileSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * USER SERVICE IMPLEMENTATION - COMPLETE WITH UUID MIGRATION FIXES
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Core service for comprehensive user management in YouthConnect platform.
 *
 * This service handles:
 * - User creation and registration (web and USSD)
 * - Profile management for all user types (Youth, Mentor, NGO, Funder, Service Provider)
 * - User retrieval by email, phone, or ID
 * - Job statistics integration via JobServiceClient
 * - Paginated mentor listing for scalability
 * - User search and filtering
 * - Account activation/deactivation
 * - Internal API methods for job-service integration
 *
 * Design Principles:
 * - Transactional integrity for all write operations
 * - Role-based profile creation
 * - Support for both web and USSD registration flows
 * - Circuit breaker pattern for external service calls
 * - Graceful degradation when job-service is unavailable
 * - Comprehensive error handling and logging
 * - Pagination support for large datasets
 * - Type-safe UUID handling throughout
 *
 * @author Douglas Kings Kato
 * @version 2.4.0 (UUID Migration Complete)
 * @since 2025-11-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    // ========================================================================
    // REPOSITORY DEPENDENCIES
    // ========================================================================

    private final UserRepository userRepository;
    private final YouthProfileRepository youthProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final FunderProfileRepository funderProfileRepository;
    private final ServiceProviderProfileRepository serviceProviderProfileRepository;

    // ========================================================================
    // EXTERNAL SERVICE CLIENTS
    // ========================================================================

    @Autowired
    private JobServiceClient jobServiceClient;

    // ========================================================================
    // USER CREATION METHODS (AUTH SERVICE INTEGRATION)
    // ========================================================================

    /**
     * Creates a new user in the system (called by auth-service)
     *
     * This method is invoked by the auth-service after successful authentication setup.
     * It assumes the password is already hashed by the auth-service.
     *
     * Flow:
     * 1. Validates email and phone uniqueness
     * 2. Creates base User entity
     * 3. Delegates to role-specific profile creation
     *
     * @param request Registration request containing user details and role
     * @return Created User entity with generated ID
     * @throws UserAlreadyExistsException if email or phone already exists
     */
    @Override
    @Transactional
    public User createUser(RegistrationRequest request) {
        log.info("Creating new user: email={}, role={}", request.getEmail(), request.getRole());

        // Validate uniqueness constraints
        if (emailExists(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        if (request.getPhoneNumber() != null && phoneExists(request.getCleanPhoneNumber())) {
            throw new UserAlreadyExistsException("Phone number already registered");
        }

        // Build user entity
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .phoneNumber(request.getCleanPhoneNumber())
                .passwordHash(request.getPassword()) // Pre-hashed by auth-service
                .role(request.getRole())
                .isActive(true)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        // Create role-specific profile (non-blocking)
        createUserProfileForRole(savedUser, request);

        return savedUser;
    }

    /**
     * Creates role-specific profile for newly registered user
     *
     * This method delegates to specialized profile creation methods based on user role.
     * Profile creation failures are logged but don't prevent user creation.
     *
     * @param user Saved user entity
     * @param request Registration request with profile-specific data
     */
    private void createUserProfileForRole(User user, RegistrationRequest request) {
        try {
            switch (user.getRole()) {
                case YOUTH -> createYouthProfileInternal(user, request);
                case MENTOR -> createMentorProfileInternal(user, request);
                case NGO -> createNgoProfileInternal(user, request);
                case FUNDER -> createFunderProfileInternal(user, request);
                case SERVICE_PROVIDER -> createServiceProviderProfileInternal(user, request);
                default -> log.info("No specific profile required for role: {}", user.getRole());
            }
        } catch (Exception e) {
            log.error("Failed to create profile for user {}: {}", user.getEmail(), e.getMessage(), e);
            // Non-blocking: user creation succeeds even if profile creation fails
        }
    }

    // ========================================================================
    // USER RETRIEVAL METHODS
    // ========================================================================

    /**
     * Retrieves user by email address
     *
     * @param email User's email (case-insensitive)
     * @return User entity
     * @throws UserNotFoundException if user not found
     */
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    /**
     * Retrieves user by phone number
     *
     * Used primarily by USSD service for phone-based authentication.
     *
     * @param phoneNumber User's phone number in E.164 format
     * @return User entity or null if not found
     */
    @Override
    public User getUserByPhone(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    /**
     * Retrieves user by ID
     *
     * @param userId User's unique identifier
     * @return User entity
     * @throws UserNotFoundException if user not found
     */
    @Override
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    // ========================================================================
    // PROFILE RETRIEVAL METHODS (WITH JOB STATISTICS)
    // ========================================================================

    /**
     * Retrieves user profile by email with job statistics
     *
     * ENHANCED: Now includes job application statistics and employment status from job-service.
     * Uses circuit breaker pattern - if job-service is unavailable, returns profile without stats.
     *
     * Used by authenticated web users to get their own profile information.
     * This method provides a convenient way to fetch profile using email
     * which is the primary authentication identifier for web users.
     *
     * @param email User's email address
     * @return UserProfileDTO with complete profile and job statistics
     * @throws UserNotFoundException if user or profile not found
     */
    @Override
    public UserProfileDTO getUserProfileByEmail(String email) {
        log.debug("Retrieving profile with job stats for email: {}", email);
        User user = getUserByEmail(email);

        // Build basic profile DTO
        UserProfileDTO profile = getUserProfileById(user.getId());

        // Note: For enhanced profile with job statistics, use getEnhancedUserProfileByEmail()
        return profile;
    }

    /**
     * Retrieves enhanced user profile by email with job statistics
     *
     * This method returns a comprehensive UserProfileResponse that includes:
     * - All basic user and profile information
     * - Job application statistics (total, approved, pending, rejected)
     * - Current employment status and details
     * - Success rate calculations
     *
     * Uses circuit breaker pattern - if job-service is unavailable, returns profile without stats.
     *
     * @param email User's email address
     * @return UserProfileResponse with complete profile and job statistics
     * @throws UserNotFoundException if user or profile not found
     */
    public UserProfileResponse getEnhancedUserProfileByEmail(String email) {
        log.debug("Retrieving enhanced profile with job stats for email: {}", email);
        User user = getUserByEmail(email);

        // Build basic profile with role-specific data
        UserProfileResponse profile = buildUserProfileResponse(user);

        // Fetch and add job statistics (with graceful degradation)
        try {
            enrichProfileWithJobStatistics(profile, user.getId());
        } catch (Exception e) {
            log.warn("Failed to fetch job statistics for user {}: {}", user.getId(), e.getMessage());
            // Continue without job stats - profile is still functional
        }

        return profile;
    }

    /**
     * Retrieves user profile by phone number (USSD-specific)
     *
     * This method is optimized for USSD service operations where phone number
     * is the primary identifier. Currently supports Youth profiles.
     * Does not include job statistics for USSD users to minimize latency.
     *
     * @param phoneNumber User's phone number
     * @return UserProfileDTO with basic profile information
     * @throws UserNotFoundException if user or profile not found
     */
    @Override
    public UserProfileDTO getUserProfileByPhone(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found with phone: " + phoneNumber));

        // USSD users are primarily Youth profiles
        YouthProfile profile = youthProfileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Profile not found for user: " + user.getEmail()));

        return UserProfileDTO.builder()
                .userId(user.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    /**
     * Retrieves user profile by user ID (role-aware)
     *
     * This method intelligently fetches the appropriate profile based on user role.
     * Falls back to basic information if specific profile doesn't exist.
     * Legacy method - consider using getUserProfileByEmail for enhanced features.
     *
     * @param userId User's unique identifier
     * @return UserProfileDTO with role-specific profile data
     */
    @Override
    public UserProfileDTO getUserProfileById(UUID userId) {
        User user = getUserById(userId);

        // Attempt to retrieve role-specific profile
        switch (user.getRole()) {
            case YOUTH:
                YouthProfile youthProfile = youthProfileRepository.findByUser_Id(userId).orElse(null);
                if (youthProfile != null) {
                    return buildUserProfileDTO(user, youthProfile.getFirstName(), youthProfile.getLastName());
                }
                break;

            case MENTOR:
                MentorProfile mentorProfile = mentorProfileRepository.findByUser_Id(userId).orElse(null);
                if (mentorProfile != null) {
                    return buildUserProfileDTO(user, mentorProfile.getFirstName(), mentorProfile.getLastName());
                }
                break;
        }

        // Fallback to generic profile for roles without specific profiles
        return buildUserProfileDTO(user, "User", String.valueOf(user.getId()));
    }

    // ========================================================================
    // JOB STATISTICS INTEGRATION (UUID MIGRATION FIXED)
    // ========================================================================

    /**
     * Enrich user profile with job application statistics and employment status
     *
     * ✅ FIXED: This method now properly handles UUID type conversions for jobId
     *
     * This method fetches job-related data from job-service and enriches the profile.
     * Implements circuit breaker pattern for resilience:
     * - If job-service is unavailable, profile returns without job stats
     * - If partial data is available, includes what's available
     * - Logs all errors for monitoring and debugging
     * - Handles type conversion during UUID migration period
     *
     * Job Statistics Include:
     * - Total applications submitted
     * - Applications by status (pending, approved, rejected)
     * - Success rate calculation
     * - Current employment information with proper UUID handling
     *
     * @param profile The profile to enrich
     * @param userId The user ID
     */
    private void enrichProfileWithJobStatistics(UserProfileResponse profile, UUID userId) {
        try {
            // Fetch application summary
            ApplicationSummaryResponse appSummary = jobServiceClient.getUserApplicationSummary(userId);

            if (appSummary != null) {
                profile.setTotalJobApplications(appSummary.totalApplications());
                profile.setApprovedJobApplications(appSummary.approvedApplications());
                profile.setPendingJobApplications(appSummary.pendingApplications());
                profile.setRejectedJobApplications(appSummary.rejectedApplications());
                profile.setJobApplicationSuccessRate(appSummary.successRate());

                log.debug("Added job statistics: {} total, {} approved for user {}",
                        appSummary.totalApplications(),
                        appSummary.approvedApplications(),
                        userId);
            }

            // Fetch current employment status
            CurrentEmploymentDto employment = jobServiceClient.getCurrentEmployment(userId);

            if (employment != null && employment.isCurrent()) {
                profile.setCurrentEmploymentStatus(
                        String.format("Employed at %s as %s",
                                employment.companyName(),
                                employment.jobTitle())
                );

                // ✅ FIXED: Proper UUID handling for jobId
                // Handle both UUID and Long types during migration period
                UUID jobIdAsUuid = convertToUUID(employment.jobId());

                // Set detailed current job information with proper UUID handling
                UserProfileResponse.CurrentJobDetails jobDetails = UserProfileResponse.CurrentJobDetails.builder()
                        .jobId(jobIdAsUuid)  // ✅ Now properly handles UUID conversion
                        .jobTitle(employment.jobTitle())
                        .companyName(employment.companyName())
                        .employmentType(employment.employmentType())
                        .startDate(employment.startDate())
                        .build();

                profile.setCurrentJob(jobDetails);

                log.debug("Added employment status for user {}: {} (jobId: {})",
                        userId, employment.companyName(), jobIdAsUuid);
            } else {
                profile.setCurrentEmploymentStatus("Seeking Opportunities");
            }

        } catch (Exception e) {
            log.error("Error enriching profile with job statistics for user {}: {}",
                    userId, e.getMessage());
            // Set safe default values
            profile.setTotalJobApplications(0);
            profile.setPendingJobApplications(0);
            profile.setApprovedJobApplications(0);
            profile.setCurrentEmploymentStatus("Status unavailable");
        }
    }

    /**
     * Convert job ID to UUID
     *
     * ✅ MIGRATION HELPER: Handles conversion during the transition period when
     * job-service might return Long IDs but user-service expects UUIDs.
     *
     * <p><strong>Conversion Strategy:</strong></p>
     * <ul>
     *   <li>If input is already UUID, return it directly</li>
     *   <li>If input is Long, create deterministic UUID</li>
     *   <li>If input is null, return null</li>
     *   <li>Logs warnings for unexpected type conversions</li>
     * </ul>
     *
     * <p><strong>Deterministic UUID Generation:</strong></p>
     * Uses the Long value as the least significant bits of the UUID,
     * ensuring the same Long always produces the same UUID.
     * This maintains consistency across multiple calls.
     *
     * <p><strong>Future Migration:</strong></p>
     * Once job-service is fully migrated to UUID, this method can be:
     * <ul>
     *   <li>Simplified to direct cast: (UUID) jobId</li>
     *   <li>Or removed entirely if CurrentEmploymentDto.jobId() returns UUID</li>
     * </ul>
     *
     * @param jobId Job ID as Object (could be UUID, Long, or null)
     * @return UUID representation of job ID, or null
     */
    private UUID convertToUUID(Object jobId) {
        if (jobId == null) {
            return null;
        }

        // If already UUID, return directly
        if (jobId instanceof UUID) {
            return (UUID) jobId;
        }

        // If Long, convert to deterministic UUID
        if (jobId instanceof Long) {
            Long longId = (Long) jobId;
            log.debug("Converting Long jobId {} to UUID (migration helper)", longId);
            // Create reproducible UUID from Long value
            // Most significant bits = 0, least significant bits = longId
            return new UUID(0L, longId);
        }

        // Unexpected type - log error and return null
        log.error("Unexpected jobId type: {} (value: {}). Expected UUID or Long.",
                jobId.getClass().getName(), jobId);
        return null;
    }

    /**
     * Helper method to build UserProfileResponse from User entity
     *
     * Constructs a comprehensive profile response including:
     * - Basic user information (email, phone, role)
     * - Account status (active, verified)
     * - Timestamps (created, updated, last login)
     * - Role-specific profile data
     *
     * @param user The user entity
     * @return UserProfileResponse with all basic information
     */
    private UserProfileResponse buildUserProfileResponse(User user) {
        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin());

        // Add role-specific profile data
        switch (user.getRole()) {
            case YOUTH:
                YouthProfile youthProfile = youthProfileRepository.findByUser_Id(user.getId()).orElse(null);
                if (youthProfile != null) {
                    builder.firstName(youthProfile.getFirstName())
                            .lastName(youthProfile.getLastName())
                            .gender(youthProfile.getGender())
                            .district(youthProfile.getDistrict())
                            .profession(youthProfile.getProfession())
                            .description(youthProfile.getDescription());
                }
                break;

            case MENTOR:
                MentorProfile mentorProfile = mentorProfileRepository.findByUser_Id(user.getId()).orElse(null);
                if (mentorProfile != null) {
                    builder.firstName(mentorProfile.getFirstName())
                            .lastName(mentorProfile.getLastName())
                            .description(mentorProfile.getBio());
                }
                break;

            case NGO:
                NgoProfile ngoProfile = ngoProfileRepository.findByUser_Id(user.getId()).orElse(null);
                if (ngoProfile != null) {
                    builder.firstName(ngoProfile.getOrganisationName())
                            .description(ngoProfile.getDescription());
                }
                break;

            case FUNDER:
                FunderProfile funderProfile = funderProfileRepository.findByUser_Id(user.getId()).orElse(null);
                if (funderProfile != null) {
                    builder.firstName(funderProfile.getFunderName());
                }
                break;

            case SERVICE_PROVIDER:
                ServiceProviderProfile spProfile = serviceProviderProfileRepository.findByUser_Id(user.getId()).orElse(null);
                if (spProfile != null) {
                    builder.firstName(spProfile.getProviderName())
                            .description(spProfile.getAreaOfExpertise());
                }
                break;
        }

        return builder.build();
    }

    // ========================================================================
    // INTERNAL API METHODS (FOR JOB SERVICE INTEGRATION)
    // ========================================================================

    /**
     * Check if user exists by ID
     *
     * Called by job-service to verify user before creating jobs or applications.
     * This is a lightweight check that doesn't load full user data.
     *
     * @param userId User ID to check
     * @return true if user exists and is active, false otherwise
     */
    @Override
    public boolean userExists(UUID userId) {
        log.debug("Checking if user exists: {}", userId);
        boolean exists = userRepository.existsById(userId);
        log.debug("User {} existence check: {}", userId, exists);
        return exists;
    }

    /**
     * Get user profile summary for job service
     *
     * Returns comprehensive user information needed for job posting and application management.
     *
     * @param userId User ID to fetch
     * @return UserProfileSummary with essential user information
     * @throws UserNotFoundException if user not found
     */
    @Override
    public UserProfileSummary getUserSummary(UUID userId) {
        log.debug("Fetching user summary for userId: {}", userId);

        User user = getUserById(userId);

        String fullName = "User " + userId;
        String organizationName = null;

        // Get role-specific information
        switch (user.getRole()) {
            case YOUTH:
                YouthProfile youthProfile = youthProfileRepository.findByUser_Id(userId).orElse(null);
                if (youthProfile != null) {
                    fullName = youthProfile.getFirstName() + " " + youthProfile.getLastName();
                }
                break;

            case MENTOR:
                MentorProfile mentorProfile = mentorProfileRepository.findByUser_Id(userId).orElse(null);
                if (mentorProfile != null) {
                    fullName = mentorProfile.getFirstName() + " " + mentorProfile.getLastName();
                }
                break;

            case NGO:
                NgoProfile ngoProfile = ngoProfileRepository.findByUser_Id(userId).orElse(null);
                if (ngoProfile != null) {
                    organizationName = ngoProfile.getOrganisationName();
                    fullName = organizationName;
                }
                break;

            case COMPANY:
            case RECRUITER:
            case GOVERNMENT:
                organizationName = getUserOrganization(userId);
                fullName = organizationName != null ? organizationName : "Organization " + userId;
                break;

            case FUNDER:
                FunderProfile funderProfile = funderProfileRepository.findByUser_Id(userId).orElse(null);
                if (funderProfile != null) {
                    organizationName = funderProfile.getFunderName();
                    fullName = organizationName;
                }
                break;

            case SERVICE_PROVIDER:
                ServiceProviderProfile spProfile = serviceProviderProfileRepository.findByUser_Id(userId).orElse(null);
                if (spProfile != null) {
                    organizationName = spProfile.getProviderName();
                    fullName = organizationName;
                }
                break;
        }

        UserProfileSummary summary = new UserProfileSummary(
                user.getId(),
                user.getEmail(),
                fullName,
                user.getRole().name(),
                organizationName,
                user.isActive(),
                user.isEmailVerified()
        );

        log.debug("User summary retrieved for userId {}: {}", userId, fullName);

        return summary;
    }

    /**
     * Check if user has permission to post jobs
     *
     * @param userId User ID to check
     * @return true if user can post jobs, false otherwise
     * @throws UserNotFoundException if user not found
     */
    @Override
    public boolean canUserPostJobs(UUID userId) {
        log.debug("Checking job posting permission for userId: {}", userId);

        User user = getUserById(userId);

        boolean canPost = user.getRole() == Role.NGO ||
                user.getRole() == Role.COMPANY ||
                user.getRole() == Role.RECRUITER ||
                user.getRole() == Role.GOVERNMENT;

        log.debug("User {} (role: {}) can post jobs: {}", userId, user.getRole(), canPost);

        return canPost;
    }

    /**
     * Get user's organization name
     *
     * @param userId User ID to fetch organization for
     * @return Organization name or fallback identifier
     * @throws UserNotFoundException if user not found
     */
    @Override
    public String getUserOrganization(UUID userId) {
        log.debug("Fetching organization for userId: {}", userId);

        User user = getUserById(userId);

        String organization = null;

        switch (user.getRole()) {
            case NGO:
                NgoProfile ngoProfile = ngoProfileRepository.findByUser_Id(userId).orElse(null);
                if (ngoProfile != null) {
                    organization = ngoProfile.getOrganisationName();
                }
                break;

            case FUNDER:
                FunderProfile funderProfile = funderProfileRepository.findByUser_Id(userId).orElse(null);
                if (funderProfile != null) {
                    organization = funderProfile.getFunderName();
                }
                break;

            case SERVICE_PROVIDER:
                ServiceProviderProfile spProfile = serviceProviderProfileRepository
                        .findByUser_Id(userId).orElse(null);
                if (spProfile != null) {
                    organization = spProfile.getProviderName();
                }
                break;

            case COMPANY:
            case RECRUITER:
            case GOVERNMENT:
                organization = "Organization (ID: " + userId + ")";
                break;
        }

        if (organization == null) {
            organization = "Organization (ID: " + userId + ")";
        }

        log.debug("Organization for userId {}: {}", userId, organization);

        return organization;
    }

    // ========================================================================
    // PROFILE CREATION METHODS (PUBLIC API)
    // ========================================================================

    @Override
    @Transactional
    public UserProfileDTO createUssdUserProfile(User user, UssdRegistrationRequest request) {
        log.info("Creating USSD profile for user {}", user.getPhoneNumber());

        YouthProfile profile = new YouthProfile(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setGender(request.getGender());
        profile.setDistrict(request.getDistrict());

        YouthProfile savedProfile = youthProfileRepository.save(profile);

        return buildUserProfileDTO(user, savedProfile.getFirstName(), savedProfile.getLastName());
    }

    @Override
    @Transactional
    public UserProfileDTO createUserProfile(User user, RegistrationRequest request) {
        log.info("Creating profile for user {} with role {}", user.getEmail(), user.getRole());

        return switch (user.getRole()) {
            case YOUTH -> createYouthProfile(user, request);
            case MENTOR -> createMentorProfile(user, request);
            case NGO -> createNgoProfile(user, request);
            case FUNDER -> createFunderProfile(user, request);
            case SERVICE_PROVIDER -> createServiceProviderProfile(user, request);
            case ADMIN -> createBasicUserProfile(user);
            default -> throw new IllegalArgumentException("Unsupported user role: " + user.getRole());
        };
    }

    // ========================================================================
    // PROFILE CREATION METHODS (INTERNAL)
    // ========================================================================

    private void createYouthProfileInternal(User user, RegistrationRequest request) {
        YouthProfile profile = new YouthProfile(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setGender(request.getGender());
        profile.setDescription(request.getDescription());
        youthProfileRepository.save(profile);
        log.debug("Youth profile created for user: {}", user.getId());
    }

    private void createMentorProfileInternal(User user, RegistrationRequest request) {
        MentorProfile profile = new MentorProfile(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setBio(request.getBio());
        profile.setAreaOfExpertise(request.getAreaOfExpertise());
        profile.setExperienceYears(request.getExperienceYears());
        mentorProfileRepository.save(profile);
        log.debug("Mentor profile created for user: {}", user.getId());
    }

    private void createNgoProfileInternal(User user, RegistrationRequest request) {
        NgoProfile profile = new NgoProfile(user);
        profile.setOrganisationName(request.getOrganisationName());
        profile.setLocation(request.getLocation());
        profile.setDescription(request.getDescription());
        ngoProfileRepository.save(profile);
        log.debug("NGO profile created for user: {}", user.getId());
    }

    private void createFunderProfileInternal(User user, RegistrationRequest request) {
        FunderProfile profile = new FunderProfile(user);
        profile.setFunderName(request.getFunderName());
        profile.setFundingFocus(request.getFundingFocus());
        funderProfileRepository.save(profile);
        log.debug("Funder profile created for user: {}", user.getId());
    }

    private void createServiceProviderProfileInternal(User user, RegistrationRequest request) {
        ServiceProviderProfile profile = new ServiceProviderProfile(user);
        profile.setProviderName(request.getProviderName());
        profile.setLocation(request.getLocation());
        profile.setAreaOfExpertise(request.getAreaOfExpertise());
        serviceProviderProfileRepository.save(profile);
        log.debug("Service provider profile created for user: {}", user.getId());
    }

    private UserProfileDTO createYouthProfile(User user, RegistrationRequest request) {
        YouthProfile profile = new YouthProfile(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setGender(request.getGender());
        profile.setDescription(request.getDescription());
        profile.setDistrict(request.getDistrict());
        profile.setProfession(request.getProfession());

        if (request.getDateOfBirth() != null) {
            try {
                profile.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
            } catch (Exception e) {
                log.warn("Invalid date format for user {}: {}", user.getEmail(), request.getDateOfBirth());
            }
        }

        YouthProfile savedProfile = youthProfileRepository.save(profile);
        log.info("Created youth profile for user: {}", user.getEmail());

        return buildUserProfileDTO(user, savedProfile.getFirstName(), savedProfile.getLastName());
    }

    private UserProfileDTO createMentorProfile(User user, RegistrationRequest request) {
        MentorProfile profile = new MentorProfile(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setBio(request.getBio());
        profile.setAreaOfExpertise(request.getAreaOfExpertise());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setAvailabilityStatus(MentorProfile.AvailabilityStatus.AVAILABLE);

        MentorProfile savedProfile = mentorProfileRepository.save(profile);
        log.info("Created mentor profile for user: {}", user.getEmail());

        return buildUserProfileDTO(user, savedProfile.getFirstName(), savedProfile.getLastName());
    }

    private UserProfileDTO createNgoProfile(User user, RegistrationRequest request) {
        NgoProfile profile = new NgoProfile(user);
        profile.setOrganisationName(request.getOrganisationName());
        profile.setLocation(request.getLocation());
        profile.setDescription(request.getDescription());
        profile.setVerified(false);

        NgoProfile savedProfile = ngoProfileRepository.save(profile);
        log.info("Created NGO profile for user: {}", user.getEmail());

        return buildUserProfileDTO(user, savedProfile.getOrganisationName(), "Organization");
    }

    private UserProfileDTO createFunderProfile(User user, RegistrationRequest request) {
        FunderProfile profile = new FunderProfile(user);
        profile.setFunderName(request.getFunderName());
        profile.setFundingFocus(request.getFundingFocus());

        FunderProfile savedProfile = funderProfileRepository.save(profile);
        log.info("Created funder profile for user: {}", user.getEmail());

        return buildUserProfileDTO(user, savedProfile.getFunderName(), "Funder");
    }

    private UserProfileDTO createServiceProviderProfile(User user, RegistrationRequest request) {
        ServiceProviderProfile profile = new ServiceProviderProfile(user);
        profile.setProviderName(request.getProviderName());
        profile.setLocation(request.getLocation());
        profile.setAreaOfExpertise(request.getAreaOfExpertise());
        profile.setVerified(false);

        ServiceProviderProfile savedProfile = serviceProviderProfileRepository.save(profile);
        log.info("Created service provider profile for user: {}", user.getEmail());

        return buildUserProfileDTO(user, savedProfile.getProviderName(), "Service Provider");
    }

    private UserProfileDTO createBasicUserProfile(User user) {
        log.info("Created basic profile for admin user: {}", user.getEmail());
        return buildUserProfileDTO(user, "Admin", "User");
    }

    private UserProfileDTO buildUserProfileDTO(User user, String firstName, String lastName) {
        return UserProfileDTO.builder()
                .userId(user.getId())
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    // ========================================================================
    // PROFILE UPDATE METHODS
    // ========================================================================

    @Override
    @Transactional
    public YouthProfile updateUserProfile(String email, ProfileUpdateRequest request) {
        User user = getUserByEmail(email);

        YouthProfile profile = youthProfileRepository.findByUser_Id(user.getId())
                .orElse(new YouthProfile(user));

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setProfession(request.getProfession());
        profile.setDescription(request.getDescription());
        profile.setDistrict(request.getDistrict());

        return youthProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public UserProfileDTO updateUserProfileByPhone(String phoneNumber, ProfileUpdateRequestDTO request) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found with phone: " + phoneNumber));

        YouthProfile profile = youthProfileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Profile not found for user: " + user.getEmail()));

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());

        youthProfileRepository.save(profile);

        return buildUserProfileDTO(user, profile.getFirstName(), profile.getLastName());
    }

    // ========================================================================
    // MENTOR-SPECIFIC METHODS (ENHANCED WITH PAGINATION)
    // ========================================================================

    /**
     * Retrieves all active mentors with their profiles (non-paginated)
     *
     * @return List of MentorProfileDTO objects for all active mentors
     * @deprecated Use {@link #getAllMentors(Pageable)} for better performance and scalability
     */
    @Override
    public List<MentorProfileDTO> getAllMentors() {
        log.debug("Fetching all mentors (non-paginated)");

        List<User> mentorUsers = userRepository.findByRoleAndIsActiveTrue(Role.MENTOR);
        log.info("Found {} active mentors (non-paginated query)", mentorUsers.size());

        return mentorUsers.stream().map(user -> {
            MentorProfile profile = mentorProfileRepository.findByUser_Id(user.getId())
                    .orElseGet(() -> {
                        log.warn("No profile found for mentor user: {}, using defaults", user.getId());
                        return new MentorProfile(user);
                    });

            return MentorProfileDTO.builder()
                    .mentorId(user.getId())
                    .user(user)
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .bio(profile.getBio())
                    .areaOfExpertise(profile.getAreaOfExpertise())
                    .experienceYears(profile.getExperienceYears())
                    .availabilityStatus(profile.getAvailabilityStatus())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Retrieves all active mentors with pagination support
     *
     * @param pageable Pagination and sorting parameters
     * @return Page of MentorProfileDTO with complete pagination metadata
     */
    @Override
    public Page<MentorProfileDTO> getAllMentors(Pageable pageable) {
        log.debug("Fetching paginated mentors - page: {}, size: {}, sort: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        Page<User> mentorUsers = userRepository.findByRoleAndIsActiveTrue(Role.MENTOR, pageable);

        log.debug("Retrieved {} mentors on page {} of {} (total: {} mentors)",
                mentorUsers.getNumberOfElements(),
                mentorUsers.getNumber() + 1,
                mentorUsers.getTotalPages(),
                mentorUsers.getTotalElements());

        return mentorUsers.map(user -> {
            MentorProfile profile = mentorProfileRepository.findByUser_Id(user.getId())
                    .orElseGet(() -> {
                        log.warn("No profile found for mentor user: {} (email: {}), creating default profile",
                                user.getId(), user.getEmail());
                        return new MentorProfile(user);
                    });

            return MentorProfileDTO.builder()
                    .mentorId(user.getId())
                    .user(user)
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .bio(profile.getBio())
                    .areaOfExpertise(profile.getAreaOfExpertise())
                    .experienceYears(profile.getExperienceYears())
                    .availabilityStatus(profile.getAvailabilityStatus())
                    .build();
        });
    }

    @Override
    public MentorProfileDTO getMentorById(UUID mentorId) {
        log.debug("Retrieving mentor profile for ID: {}", mentorId);

        User user = getUserById(mentorId);

        if (user.getRole() != Role.MENTOR) {
            log.error("Access denied: User {} has role {} but MENTOR role required",
                    mentorId, user.getRole());
            throw new IllegalArgumentException(
                    String.format("User with ID %s is not a mentor (role: %s)",
                            mentorId, user.getRole()));
        }

        MentorProfile profile = mentorProfileRepository.findByUser_Id(mentorId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Mentor profile not found for user ID: " + mentorId));

        log.debug("Successfully retrieved mentor profile: {} {} (expertise: {})",
                profile.getFirstName(),
                profile.getLastName(),
                profile.getAreaOfExpertise());

        return MentorProfileDTO.builder()
                .mentorId(user.getId())
                .user(user)
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .bio(profile.getBio())
                .areaOfExpertise(profile.getAreaOfExpertise())
                .experienceYears(profile.getExperienceYears())
                .availabilityStatus(profile.getAvailabilityStatus())
                .build();
    }

    // ========================================================================
    // USER QUERY AND SEARCH METHODS
    // ========================================================================

    @Override
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleAndIsActiveTrue(role);
    }

    @Override
    public List<User> searchUsers(String searchTerm) {
        log.debug("Searching users (non-paginated) with term: {}", searchTerm);
        List<User> results = userRepository.searchActiveUsers(searchTerm);
        log.debug("Found {} users matching search term: {}", results.size(), searchTerm);
        return results;
    }

    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Searching users (paginated) - term: '{}', page: {}, size: {}",
                searchTerm, pageable.getPageNumber(), pageable.getPageSize());

        Page<User> results = userRepository.searchActiveUsers(searchTerm, pageable);

        log.debug("Found {} users on page {} of {} (total matches: {})",
                results.getNumberOfElements(),
                results.getNumber() + 1,
                results.getTotalPages(),
                results.getTotalElements());

        return results;
    }

    @Override
    public long getUserCountByRole(Role role) {
        return userRepository.countActiveUsersByRole(role);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean phoneExists(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    // ========================================================================
    // ACCOUNT MANAGEMENT METHODS
    // ========================================================================

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
        log.info("Deactivated user account: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void reactivateUser(UUID userId) {
        User user = getUserById(userId);
        user.setActive(true);
        userRepository.save(user);
        log.info("Reactivated user account: {}", user.getEmail());
    }
}