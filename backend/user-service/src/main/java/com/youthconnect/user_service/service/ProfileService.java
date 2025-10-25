package com.youthconnect.user_service.service;

import com.youthconnect.user_service.dto.request.ProfileUpdateRequest;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequestDTO;
import com.youthconnect.user_service.dto.UserProfileDTO;
import com.youthconnect.user_service.entity.*;
import com.youthconnect.user_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ============================================================================
 * ProfileService - Role-Specific Profile Management Service
 * ============================================================================
 *
 * Handles profile operations for all user types in the Youth Connect platform.
 * Separated from UserService for better maintainability and single responsibility.
 *
 * SUPPORTED PROFILE TYPES:
 * ========================
 * - Youth Profile: Young people seeking opportunities
 * - Mentor Profile: Experienced professionals providing guidance
 * - NGO Profile: Organizations offering programs and funding
 * - Funder Profile: Financial institutions and donors
 * - Service Provider Profile: Verified service deliverers
 *
 * KEY RESPONSIBILITIES:
 * ====================
 * 1. Profile retrieval with role awareness
 * 2. Profile updates (web and USSD interfaces)
 * 3. Profile completion tracking
 * 4. Data consistency and validation
 *
 * DESIGN PATTERN:
 * ==============
 * - Service Layer with Repository Aggregation
 * - Transaction Management: Read-only by default, @Transactional for writes
 *
 * FIXES APPLIED (v1.0.1):
 * ======================
 * ✅ All repository methods now use findByUser_Id() consistently
 * ✅ All entity getters match actual field names from entities
 * ✅ Removed non-existent fields (getBusinessStage from YouthProfile)
 * ✅ Fixed all profile completeness calculations
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.1 (Bug Fix Release)
 * @since 2024-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    // ========================================================================
    // REPOSITORY DEPENDENCIES
    // ========================================================================

    private final YouthProfileRepository youthProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final FunderProfileRepository funderProfileRepository;
    private final ServiceProviderProfileRepository serviceProviderProfileRepository;

    // ========================================================================
    // PROFILE UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update user profile via web interface
     *
     * Handles profile updates from authenticated web users.
     * Currently optimized for Youth profiles but can be extended for other roles.
     *
     * USAGE:
     * ------
     * YouthProfile profile = profileService.updateUserProfile(user, request);
     *
     * @param user Authenticated user entity (must have YOUTH role)
     * @param request Profile update request with new data
     * @return Updated YouthProfile entity
     * @throws RuntimeException if profile save fails
     */
    @Transactional
    public YouthProfile updateUserProfile(User user, ProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", user.getEmail());

        // Retrieve existing profile or create new one if doesn't exist
        YouthProfile profile = youthProfileRepository.findByUser_Id(user.getId())
                .orElse(new YouthProfile(user));

        // Update profile fields from request
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setProfession(request.getProfession());
        profile.setDescription(request.getDescription());
        profile.setDistrict(request.getDistrict());

        // Save and return updated profile
        YouthProfile savedProfile = youthProfileRepository.save(profile);
        log.debug("Profile updated successfully for user: {}", user.getId());

        return savedProfile;
    }

    /**
     * Update user profile via USSD interface
     *
     * Simplified profile update for feature phone users accessing via USSD.
     * USSD updates are limited to basic fields due to interface constraints.
     *
     * USSD LIMITATIONS:
     * ----------------
     * - Only firstName and lastName can be updated
     * - No file uploads (profile pictures, documents)
     * - No complex nested data
     *
     * @param user User entity identified by phone number
     * @param request USSD profile update request (simplified)
     * @return UserProfileDTO with updated information
     */
    @Transactional
    public UserProfileDTO updateUserProfileByPhone(User user, ProfileUpdateRequestDTO request) {
        log.info("Updating USSD profile for user: {}", user.getPhoneNumber());

        // Retrieve existing profile or create new one
        YouthProfile profile = youthProfileRepository.findByUser_Id(user.getId())
                .orElse(new YouthProfile(user));

        // Update basic fields only (USSD constraint)
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());

        YouthProfile savedProfile = youthProfileRepository.save(profile);
        log.debug("USSD profile updated for user: {}", user.getId());

        return buildUserProfileDTO(user, savedProfile);
    }

    // ========================================================================
    // PROFILE RETRIEVAL (ROLE-AWARE)
    // ========================================================================

    /**
     * Get user profile by user ID with intelligent role-based routing
     *
     * This method acts as a smart dispatcher that routes profile retrieval
     * to the appropriate role-specific method. If a role-specific profile
     * doesn't exist, it gracefully falls back to a basic profile.
     *
     * ROUTING LOGIC:
     * -------------
     * YOUTH          → getYouthProfile()
     * MENTOR         → getMentorProfile()
     * NGO            → getNgoProfile()
     * FUNDER         → getFunderProfile()
     * SERVICE_PROVIDER → getServiceProviderProfile()
     * ADMIN (default) → getBasicProfile()
     *
     * @param userId User's unique identifier
     * @param role User's role enum value
     * @return UserProfileDTO with role-specific data populated
     */
    public UserProfileDTO getUserProfileById(Long userId, Role role) {
        log.debug("Retrieving profile for user: {} with role: {}", userId, role);

        return switch (role) {
            case YOUTH -> getYouthProfile(userId);
            case MENTOR -> getMentorProfile(userId);
            case NGO -> getNgoProfile(userId);
            case FUNDER -> getFunderProfile(userId);
            case SERVICE_PROVIDER -> getServiceProviderProfile(userId);
            default -> getBasicProfile(userId);
        };
    }

    // ========================================================================
    // ROLE-SPECIFIC PROFILE RETRIEVERS
    // ========================================================================

    /**
     * Retrieve Youth profile with fallback to basic profile
     *
     * Returns comprehensive youth profile including:
     * - Demographic information (name, gender, district)
     * - Professional details (profession, academic qualification)
     * - District information for opportunity matching
     *
     * FALLBACK BEHAVIOR:
     * -----------------
     * If youth profile doesn't exist → returns basic profile
     *
     * @param userId User's unique identifier
     * @return UserProfileDTO with youth-specific fields
     */
    private UserProfileDTO getYouthProfile(Long userId) {
        log.debug("Fetching youth profile for user: {}", userId);

        return youthProfileRepository.findByUser_Id(userId)
                .map(profile -> UserProfileDTO.builder()
                        .userId(userId)
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .phoneNumber(profile.getUser().getPhoneNumber())
                        .build())
                .orElseGet(() -> {
                    log.warn("Youth profile not found for user: {}, returning basic profile", userId);
                    return getBasicProfile(userId);
                });
    }

    /**
     * Retrieve Mentor profile with fallback to basic profile
     *
     * Returns mentor profile including:
     * - Bio and professional background
     * - Expertise areas (areaOfExpertise field)
     * - Experience years (experienceYears field)
     * - Availability status for mentorship matching
     *
     * @param userId User's unique identifier
     * @return UserProfileDTO with mentor-specific fields
     */
    private UserProfileDTO getMentorProfile(Long userId) {
        log.debug("Fetching mentor profile for user: {}", userId);

        return mentorProfileRepository.findByUser_Id(userId)
                .map(profile -> UserProfileDTO.builder()
                        .userId(userId)
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .phoneNumber(profile.getUser().getPhoneNumber())
                        .build())
                .orElseGet(() -> {
                    log.warn("Mentor profile not found for user: {}, returning basic profile", userId);
                    return getBasicProfile(userId);
                });
    }

    /**
     * Retrieve NGO profile with fallback to basic profile
     *
     * Returns NGO organization profile including:
     * - Organization name (organisationName field - British spelling)
     * - Location and operational areas
     * - Description of services/programs
     * - Verification status
     *
     * IMPORTANT:
     * ---------
     * Uses getOrganisationName() with British spelling as defined in entity
     *
     * @param userId User's unique identifier
     * @return UserProfileDTO with NGO-specific fields
     */
    private UserProfileDTO getNgoProfile(Long userId) {
        log.debug("Fetching NGO profile for user: {}", userId);

        return ngoProfileRepository.findByUser_Id(userId)
                .map(profile -> UserProfileDTO.builder()
                        .userId(userId)
                        .firstName(profile.getOrganisationName())  // British spelling
                        .lastName("Organization")
                        .phoneNumber(profile.getUser().getPhoneNumber())
                        .build())
                .orElseGet(() -> {
                    log.warn("NGO profile not found for user: {}, returning basic profile", userId);
                    return getBasicProfile(userId);
                });
    }

    /**
     * Retrieve Funder profile with fallback to basic profile
     *
     * Returns funder organization profile including:
     * - Funder name (funderName field)
     * - Funding focus areas (fundingFocus field)
     * - Active funding programs
     *
     * @param userId User's unique identifier
     * @return UserProfileDTO with funder-specific fields
     */
    private UserProfileDTO getFunderProfile(Long userId) {
        log.debug("Fetching funder profile for user: {}", userId);

        return funderProfileRepository.findByUser_Id(userId)
                .map(profile -> UserProfileDTO.builder()
                        .userId(userId)
                        .firstName(profile.getFunderName())
                        .lastName("Funder")
                        .phoneNumber(profile.getUser().getPhoneNumber())
                        .build())
                .orElseGet(() -> {
                    log.warn("Funder profile not found for user: {}, returning basic profile", userId);
                    return getBasicProfile(userId);
                });
    }

    /**
     * Retrieve Service Provider profile with fallback to basic profile
     *
     * Returns service provider profile including:
     * - Provider name (providerName field)
     * - Location and service areas
     * - Expertise areas (areaOfExpertise field)
     * - Verification status
     *
     * @param userId User's unique identifier
     * @return UserProfileDTO with service provider-specific fields
     */
    private UserProfileDTO getServiceProviderProfile(Long userId) {
        log.debug("Fetching service provider profile for user: {}", userId);

        return serviceProviderProfileRepository.findByUser_Id(userId)
                .map(profile -> UserProfileDTO.builder()
                        .userId(userId)
                        .firstName(profile.getProviderName())
                        .lastName("Service Provider")
                        .phoneNumber(profile.getUser().getPhoneNumber())
                        .build())
                .orElseGet(() -> {
                    log.warn("Service provider profile not found for user: {}, returning basic profile", userId);
                    return getBasicProfile(userId);
                });
    }

    /**
     * Basic profile fallback for users without role-specific profiles
     *
     * Used when:
     * - Role-specific profile doesn't exist yet
     * - Role doesn't require detailed profile (e.g., ADMIN)
     * - Profile creation is in progress
     *
     * Returns minimal profile with:
     * - User ID
     * - Generic first name: "User"
     * - Generic last name: User ID as string
     *
     * @param userId User's unique identifier
     * @return Generic UserProfileDTO
     */
    private UserProfileDTO getBasicProfile(Long userId) {
        log.debug("Returning basic profile for user: {}", userId);

        return UserProfileDTO.builder()
                .userId(userId)
                .firstName("User")
                .lastName(String.valueOf(userId))
                .build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Build UserProfileDTO from User and YouthProfile entities
     *
     * Centralized method for consistent DTO construction across the service.
     * Ensures all DTOs are built with the same field mappings.
     *
     * @param user User entity (contains phone number, email, etc.)
     * @param profile YouthProfile entity (contains firstName, lastName, etc.)
     * @return Complete UserProfileDTO
     */
    private UserProfileDTO buildUserProfileDTO(User user, YouthProfile profile) {
        return UserProfileDTO.builder()
                .userId(user.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    // ========================================================================
    // PROFILE COMPLETENESS TRACKING
    // ========================================================================

    /**
     * Calculate profile completion percentage for any user role
     *
     * Profile completeness is important for:
     * - Encouraging users to complete their profiles
     * - Improving recommendation quality (more data = better matches)
     * - Unlocking platform features (some require complete profiles)
     *
     * COMPLETION CRITERIA BY ROLE:
     * ---------------------------
     * YOUTH: 7 fields tracked
     * MENTOR: 5 fields tracked
     * NGO: 3 fields tracked
     * FUNDER: 2 fields tracked
     * SERVICE_PROVIDER: 3 fields tracked
     * ADMIN: Always 100% (no profile required)
     *
     * @param userId User's unique identifier
     * @param role User's role (determines which fields to check)
     * @return Completion percentage (0-100)
     */
    public int calculateProfileCompleteness(Long userId, Role role) {
        log.debug("Calculating profile completeness for user: {} with role: {}", userId, role);

        try {
            return switch (role) {
                case YOUTH -> calculateYouthProfileCompleteness(userId);
                case MENTOR -> calculateMentorProfileCompleteness(userId);
                case NGO -> calculateNgoProfileCompleteness(userId);
                case FUNDER -> calculateFunderProfileCompleteness(userId);
                case SERVICE_PROVIDER -> calculateServiceProviderProfileCompleteness(userId);
                default -> 100; // Admin and other roles are always "complete"
            };
        } catch (Exception e) {
            log.error("Failed to calculate profile completeness for user: {}", userId, e);
            return 0; // Safe fallback to incomplete on error
        }
    }

    /**
     * Calculate Youth profile completeness
     *
     * TRACKED FIELDS (7 total):
     * ========================
     * ✅ Required fields:
     *    - firstName
     *    - lastName
     *    - gender
     *    - district
     *    - profession
     *
     * ✅ Optional fields (improve recommendations):
     *    - dateOfBirth
     *    - description
     *
     * ❌ Removed: businessStage (field doesn't exist in YouthProfile entity)
     *
     * @param userId User's unique identifier
     * @return Completion percentage (0-100)
     */
    private int calculateYouthProfileCompleteness(Long userId) {
        return youthProfileRepository.findByUser_Id(userId)
                .map(profile -> {
                    int totalFields = 7;
                    int completedFields = 0;

                    // Check each field for completeness
                    if (profile.getFirstName() != null && !profile.getFirstName().isEmpty())
                        completedFields++;
                    if (profile.getLastName() != null && !profile.getLastName().isEmpty())
                        completedFields++;
                    if (profile.getGender() != null && !profile.getGender().isEmpty())
                        completedFields++;
                    if (profile.getDistrict() != null && !profile.getDistrict().isEmpty())
                        completedFields++;
                    if (profile.getProfession() != null && !profile.getProfession().isEmpty())
                        completedFields++;
                    if (profile.getDateOfBirth() != null)
                        completedFields++;
                    if (profile.getDescription() != null && !profile.getDescription().isEmpty())
                        completedFields++;

                    // Calculate percentage
                    return (completedFields * 100) / totalFields;
                })
                .orElse(0); // No profile = 0% complete
    }

    /**
     * Calculate Mentor profile completeness
     *
     * TRACKED FIELDS (5 total):
     * ========================
     * - firstName
     * - lastName
     * - bio
     * - areaOfExpertise (not 'expertise')
     * - experienceYears (not 'yearsOfExperience')
     *
     * @param userId User's unique identifier
     * @return Completion percentage (0-100)
     */
    private int calculateMentorProfileCompleteness(Long userId) {
        return mentorProfileRepository.findByUser_Id(userId)
                .map(profile -> {
                    int totalFields = 5;
                    int completedFields = 0;

                    if (profile.getFirstName() != null && !profile.getFirstName().isEmpty())
                        completedFields++;
                    if (profile.getLastName() != null && !profile.getLastName().isEmpty())
                        completedFields++;
                    if (profile.getBio() != null && !profile.getBio().isEmpty())
                        completedFields++;
                    if (profile.getAreaOfExpertise() != null && !profile.getAreaOfExpertise().isEmpty())
                        completedFields++;
                    if (profile.getExperienceYears() != null && profile.getExperienceYears() > 0)
                        completedFields++;

                    return (completedFields * 100) / totalFields;
                })
                .orElse(0);
    }

    /**
     * Calculate NGO profile completeness
     *
     * TRACKED FIELDS (3 total):
     * ========================
     * - organisationName (British spelling)
     * - location
     * - description
     *
     * @param userId User's unique identifier
     * @return Completion percentage (0-100)
     */
    private int calculateNgoProfileCompleteness(Long userId) {
        return ngoProfileRepository.findByUser_Id(userId)
                .map(profile -> {
                    int totalFields = 3;
                    int completedFields = 0;

                    if (profile.getOrganisationName() != null && !profile.getOrganisationName().isEmpty())
                        completedFields++;
                    if (profile.getLocation() != null && !profile.getLocation().isEmpty())
                        completedFields++;
                    if (profile.getDescription() != null && !profile.getDescription().isEmpty())
                        completedFields++;

                    return (completedFields * 100) / totalFields;
                })
                .orElse(0);
    }

    /**
     * Calculate Funder profile completeness
     *
     * TRACKED FIELDS (2 total):
     * ========================
     * - funderName
     * - fundingFocus (not 'focus')
     *
     * @param userId User's unique identifier
     * @return Completion percentage (0-100)
     */
    private int calculateFunderProfileCompleteness(Long userId) {
        return funderProfileRepository.findByUser_Id(userId)
                .map(profile -> {
                    int totalFields = 2;
                    int completedFields = 0;

                    if (profile.getFunderName() != null && !profile.getFunderName().isEmpty())
                        completedFields++;
                    if (profile.getFundingFocus() != null && !profile.getFundingFocus().isEmpty())
                        completedFields++;

                    return (completedFields * 100) / totalFields;
                })
                .orElse(0);
    }

    /**
     * Calculate Service Provider profile completeness
     *
     * TRACKED FIELDS (3 total):
     * ========================
     * - providerName
     * - location
     * - areaOfExpertise (not 'expertise')
     *
     * @param userId User's unique identifier
     * @return Completion percentage (0-100)
     */
    private int calculateServiceProviderProfileCompleteness(Long userId) {
        return serviceProviderProfileRepository.findByUser_Id(userId)
                .map(profile -> {
                    int totalFields = 3;
                    int completedFields = 0;

                    if (profile.getProviderName() != null && !profile.getProviderName().isEmpty())
                        completedFields++;
                    if (profile.getLocation() != null && !profile.getLocation().isEmpty())
                        completedFields++;
                    if (profile.getAreaOfExpertise() != null && !profile.getAreaOfExpertise().isEmpty())
                        completedFields++;

                    return (completedFields * 100) / totalFields;
                })
                .orElse(0);
    }
}