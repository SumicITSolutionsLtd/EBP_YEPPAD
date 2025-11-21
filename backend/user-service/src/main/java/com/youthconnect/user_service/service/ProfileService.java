package com.youthconnect.user_service.service;

import com.youthconnect.user_service.dto.request.ProfileUpdateRequest;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequestDTO;
import com.youthconnect.user_service.dto.UserProfileDTO;
import com.youthconnect.user_service.entity.*;
import com.youthconnect.user_service.exception.UserNotFoundException;
import com.youthconnect.user_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * ============================================================================
 * ProfileService - Role-Specific Profile Management Service (ENHANCED v1.0.2)
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
 * ENHANCEMENTS IN v1.0.2:
 * ======================
 * ✅ Added comprehensive null checks and input validation
 * ✅ Enhanced error handling with UserNotFoundException
 * ✅ Added role-based access control for updates
 * ✅ Improved logging for better debugging
 * ✅ String trimming to prevent whitespace issues
 * ✅ Better fallback mechanisms for missing profiles
 * ✅ Transaction management for atomic operations
 *
 * @author Douglas Kings Kato
 * @version 1.0.2
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
     * Handles profile updates from authenticated web users with validation.
     *
     * ENHANCEMENTS:
     * - Null checks for user and request
     * - Role validation (YOUTH only)
     * - String trimming to prevent whitespace issues
     * - Enhanced error messages
     * - Atomic transaction handling
     *
     * @param user Authenticated user entity (must have YOUTH role)
     * @param request Profile update request with new data
     * @return Updated YouthProfile entity
     * @throws IllegalArgumentException if user role is invalid or inputs are null
     * @throws UserNotFoundException if profile not found
     */
    @Transactional
    public YouthProfile updateUserProfile(User user, ProfileUpdateRequest request) {
        // Input validation
        if (user == null) {
            log.error("Attempted profile update with null user");
            throw new IllegalArgumentException("User cannot be null");
        }
        if (request == null) {
            log.error("Attempted profile update with null request for user: {}", user.getId());
            throw new IllegalArgumentException("Profile update request cannot be null");
        }

        log.info("Updating profile for user: {} (ID: {})", user.getEmail(), user.getId());

        // Role-based access control
        if (!Role.YOUTH.equals(user.getRole())) {
            log.warn("Unauthorized profile update attempt for user {} with role {}",
                    user.getId(), user.getRole());
            throw new IllegalArgumentException(
                    "Profile update only supported for YOUTH role. Current role: " + user.getRole());
        }

        // Retrieve existing profile or throw exception
        YouthProfile profile = youthProfileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> {
                    log.error("Youth profile not found for user: {}", user.getId());
                    return new UserNotFoundException("Youth profile not found for user: " + user.getId());
                });

        // Update fields with validation and trimming
        if (StringUtils.hasText(request.getFirstName())) {
            profile.setFirstName(request.getFirstName().trim());
        }
        if (StringUtils.hasText(request.getLastName())) {
            profile.setLastName(request.getLastName().trim());
        }
        if (StringUtils.hasText(request.getProfession())) {
            profile.setProfession(request.getProfession().trim());
        }
        if (StringUtils.hasText(request.getDescription())) {
            profile.setDescription(request.getDescription().trim());
        }
        if (StringUtils.hasText(request.getDistrict())) {
            profile.setDistrict(request.getDistrict().trim());
        }

        // Save and return
        YouthProfile savedProfile = youthProfileRepository.save(profile);
        log.info("Profile updated successfully for user: {} (ID: {})", user.getEmail(), user.getId());
        return savedProfile;
    }

    /**
     * Update user profile via USSD interface
     *
     * Simplified profile update for feature phone users.
     * Limited to basic fields due to USSD interface constraints.
     *
     * ENHANCEMENTS:
     * - Null checks for inputs
     * - Creates profile if missing (USSD-friendly)
     * - String trimming for clean data
     * - Enhanced logging
     *
     * @param user User entity identified by phone number
     * @param request USSD profile update request
     * @return UserProfileDTO with updated information
     * @throws IllegalArgumentException if inputs are null
     */
    @Transactional
    public UserProfileDTO updateUserProfileByPhone(User user, ProfileUpdateRequestDTO request) {
        // Input validation
        if (user == null) {
            log.error("Attempted USSD profile update with null user");
            throw new IllegalArgumentException("User cannot be null");
        }
        if (request == null) {
            log.error("Attempted USSD profile update with null request for user: {}", user.getId());
            throw new IllegalArgumentException("USSD profile update request cannot be null");
        }

        log.info("Updating USSD profile for user: {} (Phone: {})", user.getId(), user.getPhoneNumber());

        // Retrieve or create profile (USSD may create if missing)
        YouthProfile profile = youthProfileRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    log.info("Creating new youth profile for USSD user: {}", user.getId());
                    return new YouthProfile(user);
                });

        // Update basic fields only (USSD limitation)
        if (StringUtils.hasText(request.getFirstName())) {
            profile.setFirstName(request.getFirstName().trim());
        }
        if (StringUtils.hasText(request.getLastName())) {
            profile.setLastName(request.getLastName().trim());
        }

        YouthProfile savedProfile = youthProfileRepository.save(profile);
        log.info("USSD profile updated successfully for user: {} (Phone: {})",
                user.getId(), user.getPhoneNumber());

        return buildUserProfileDTO(user, savedProfile);
    }

    // ========================================================================
    // PROFILE RETRIEVAL (ROLE-AWARE)
    // ========================================================================

    /**
     * Get user profile by user ID with intelligent role-based routing
     *
     * ENHANCEMENTS:
     * - Null checks for inputs
     * - Improved logging
     * - Better error messages
     *
     * @param userId User's unique identifier
     * @param role User's role enum value
     * @return UserProfileDTO with role-specific data populated
     * @throws IllegalArgumentException if inputs are null
     */
    public UserProfileDTO getUserProfileById(UUID userId, Role role) {
        if (userId == null) {
            log.error("Attempted to retrieve profile with null userId");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (role == null) {
            log.error("Attempted to retrieve profile with null role for userId: {}", userId);
            throw new IllegalArgumentException("Role cannot be null");
        }

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
    // ROLE-SPECIFIC PROFILE RETRIEVERS (WITH ENHANCED ERROR HANDLING)
    // ========================================================================

    private UserProfileDTO getYouthProfile(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

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

    private UserProfileDTO getMentorProfile(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

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

    private UserProfileDTO getNgoProfile(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return ngoProfileRepository.findByUser_Id(userId)
                .map(profile -> UserProfileDTO.builder()
                        .userId(userId)
                        .firstName(profile.getOrganisationName())
                        .lastName("Organization")
                        .phoneNumber(profile.getUser().getPhoneNumber())
                        .build())
                .orElseGet(() -> {
                    log.warn("NGO profile not found for user: {}, returning basic profile", userId);
                    return getBasicProfile(userId);
                });
    }

    private UserProfileDTO getFunderProfile(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

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

    private UserProfileDTO getServiceProviderProfile(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

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

    private UserProfileDTO getBasicProfile(UUID userId) {
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
     * ENHANCEMENTS:
     * - Added null check for user
     * - Safe null handling for profile fields
     */
    private UserProfileDTO buildUserProfileDTO(User user, YouthProfile profile) {
        if (user == null) {
            log.error("Attempted to build UserProfileDTO with null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        return UserProfileDTO.builder()
                .userId(user.getId())
                .firstName(profile != null ? profile.getFirstName() : null)
                .lastName(profile != null ? profile.getLastName() : null)
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    // ========================================================================
    // PROFILE COMPLETENESS TRACKING
    // ========================================================================

    /**
     * Calculate profile completion percentage for any user role
     *
     * ENHANCEMENTS:
     * - Added null checks
     * - Better error handling with try-catch
     * - Safe fallback to 0% on error
     *
     * @param userId User's unique identifier
     * @param role User's role
     * @return Completion percentage (0-100)
     */
    public int calculateProfileCompleteness(UUID userId, Role role) {
        if (userId == null) {
            log.error("Attempted to calculate completeness with null userId");
            return 0;
        }
        if (role == null) {
            log.error("Attempted to calculate completeness with null role for userId: {}", userId);
            return 0;
        }

        log.debug("Calculating profile completeness for user: {} with role: {}", userId, role);

        try {
            return switch (role) {
                case YOUTH -> calculateYouthProfileCompleteness(userId);
                case MENTOR -> calculateMentorProfileCompleteness(userId);
                case NGO -> calculateNgoProfileCompleteness(userId);
                case FUNDER -> calculateFunderProfileCompleteness(userId);
                case SERVICE_PROVIDER -> calculateServiceProviderProfileCompleteness(userId);
                default -> 100;
            };
        } catch (Exception e) {
            log.error("Failed to calculate profile completeness for user: {}", userId, e);
            return 0;
        }
    }

    private int calculateYouthProfileCompleteness(UUID userId) {
        return youthProfileRepository.findByUser_Id(userId)
                .map(profile -> {
                    int totalFields = 7;
                    int completedFields = 0;

                    if (StringUtils.hasText(profile.getFirstName())) completedFields++;
                    if (StringUtils.hasText(profile.getLastName())) completedFields++;
                    if (StringUtils.hasText(profile.getGender())) completedFields++;
                    if (StringUtils.hasText(profile.getDistrict())) completedFields++;
                    if (StringUtils.hasText(profile.getProfession())) completedFields++;
                    if (profile.getDateOfBirth() != null) completedFields++;
                    if (StringUtils.hasText(profile.getDescription())) completedFields++;

                    return (completedFields * 100) / totalFields;
                })
                .orElse(0);
    }

    private int calculateMentorProfileCompleteness(UUID userId) {
        return mentorProfileRepository.findByUser_Id(userId)
                .map(profile -> {
                    int totalFields = 5;
                    int completedFields = 0;

                    if (StringUtils.hasText(profile.getFirstName())) completedFields++;
                    if (StringUtils.hasText(profile.getLastName())) completedFields++;
                    if (StringUtils.hasText(profile.getBio())) completedFields++;
                    if (StringUtils.hasText(profile.getAreaOfExpertise())) completedFields++;
                    if (profile.getExperienceYears() != null && profile.getExperienceYears() > 0)
                        completedFields++;

                    return (completedFields * 100) / totalFields;
                })
                .orElse(0);
    }

    private int calculateNgoProfileCompleteness(UUID userId) {
        return ngoProfileRepository.findByUser_Id(userId)
                .map(profile -> {
                    int totalFields = 3;
                    int completedFields = 0;

                    if (StringUtils.hasText(profile.getOrganisationName())) completedFields++;
                    if (StringUtils.hasText(profile.getLocation())) completedFields++;
                    if (StringUtils.hasText(profile.getDescription())) completedFields++;

                    return (completedFields * 100) / totalFields;
                })
                .orElse(0);
    }

    private int calculateFunderProfileCompleteness(UUID userId) {
        return funderProfileRepository.findByUser_Id(userId)
                .map(profile -> {
                    int totalFields = 2;
                    int completedFields = 0;

                    if (StringUtils.hasText(profile.getFunderName())) completedFields++;
                    if (StringUtils.hasText(profile.getFundingFocus())) completedFields++;

                    return (completedFields * 100) / totalFields;
                })
                .orElse(0);
    }

    private int calculateServiceProviderProfileCompleteness(UUID userId) {
        return serviceProviderProfileRepository.findByUser_Id(userId)
                .map(profile -> {
                    int totalFields = 3;
                    int completedFields = 0;

                    if (StringUtils.hasText(profile.getProviderName())) completedFields++;
                    if (StringUtils.hasText(profile.getLocation())) completedFields++;
                    if (StringUtils.hasText(profile.getAreaOfExpertise())) completedFields++;

                    return (completedFields * 100) / totalFields;
                })
                .orElse(0);
    }
}