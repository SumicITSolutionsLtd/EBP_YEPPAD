package com.youthconnect.user_service.service.impl;

import com.youthconnect.user_service.dto.response.MentorProfileDTO;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserService - Core service for user management in YouthConnect platform
 *
 * This service handles:
 * - User creation and registration (web and USSD)
 * - Profile management for all user types (Youth, Mentor, NGO, Funder, Service Provider)
 * - User retrieval by email, phone, or ID
 * - User search and filtering
 * - Account activation/deactivation
 *
 * Design Principles:
 * - Transactional integrity for all write operations
 * - Role-based profile creation
 * - Support for both web and USSD registration flows
 * - Comprehensive error handling and logging
 *
 * @author YouthConnect Development Team
 * @version 2.0
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
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    // ========================================================================
    // PROFILE RETRIEVAL METHODS
    // ========================================================================

    /**
     * Retrieves user profile by email
     * FIXED: Added missing method implementation
     *
     * Used by authenticated web users to get their own profile information.
     * This method provides a convenient way to fetch profile using email
     * which is the primary authentication identifier for web users.
     *
     * @param email User's email address
     * @return UserProfileDTO with profile information
     * @throws UserNotFoundException if user or profile not found
     */
    @Override
    public UserProfileDTO getUserProfileByEmail(String email) {
        log.debug("Retrieving profile by email: {}", email);
        User user = getUserByEmail(email);
        return getUserProfileById(user.getId());
    }

    /**
     * Retrieves user profile by phone number (USSD-specific)
     *
     * This method is optimized for USSD service operations where phone number
     * is the primary identifier. Currently supports Youth profiles.
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
     *
     * @param userId User's unique identifier
     * @return UserProfileDTO with role-specific profile data
     */
    @Override
    public UserProfileDTO getUserProfileById(Long userId) {
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
    // PROFILE CREATION METHODS (PUBLIC API)
    // ========================================================================

    /**
     * Creates USSD user profile (always Youth)
     *
     * Simplified profile creation for USSD registration flow.
     * All USSD users are registered as Youth with minimal required information.
     *
     * @param user User entity
     * @param request USSD registration request
     * @return UserProfileDTO with basic profile information
     */
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

    /**
     * Creates user profile after registration (web-based flow)
     *
     * Public API method that creates comprehensive profiles with full validation
     * and enhanced features like verification status and availability.
     *
     * @param user User entity
     * @param request Registration request with profile data
     * @return UserProfileDTO containing created profile information
     * @throws IllegalArgumentException if role is unsupported
     */
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

    /**
     * Creates Youth profile during user registration
     * Internal method called by createUserProfileForRole
     */
    private void createYouthProfileInternal(User user, RegistrationRequest request) {
        YouthProfile profile = new YouthProfile(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setGender(request.getGender());
        profile.setDescription(request.getDescription());
        youthProfileRepository.save(profile);
        log.debug("Youth profile created for user: {}", user.getId());
    }

    /**
     * Creates Mentor profile during user registration
     * Internal method called by createUserProfileForRole
     */
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

    /**
     * Creates NGO profile during user registration
     * Internal method called by createUserProfileForRole
     */
    private void createNgoProfileInternal(User user, RegistrationRequest request) {
        NgoProfile profile = new NgoProfile(user);
        profile.setOrganisationName(request.getOrganisationName());
        profile.setLocation(request.getLocation());
        profile.setDescription(request.getDescription());
        ngoProfileRepository.save(profile);
        log.debug("NGO profile created for user: {}", user.getId());
    }

    /**
     * Creates Funder profile during user registration
     * Internal method called by createUserProfileForRole
     */
    private void createFunderProfileInternal(User user, RegistrationRequest request) {
        FunderProfile profile = new FunderProfile(user);
        profile.setFunderName(request.getFunderName());
        profile.setFundingFocus(request.getFundingFocus());
        funderProfileRepository.save(profile);
        log.debug("Funder profile created for user: {}", user.getId());
    }

    /**
     * Creates Service Provider profile during user registration
     * Internal method called by createUserProfileForRole
     */
    private void createServiceProviderProfileInternal(User user, RegistrationRequest request) {
        ServiceProviderProfile profile = new ServiceProviderProfile(user);
        profile.setProviderName(request.getProviderName());
        profile.setLocation(request.getLocation());
        profile.setAreaOfExpertise(request.getAreaOfExpertise());
        serviceProviderProfileRepository.save(profile);
        log.debug("Service provider profile created for user: {}", user.getId());
    }

    /**
     * Creates Youth profile with enhanced features
     * Includes date of birth parsing and comprehensive field mapping
     */
    private UserProfileDTO createYouthProfile(User user, RegistrationRequest request) {
        YouthProfile profile = new YouthProfile(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setGender(request.getGender());
        profile.setDescription(request.getDescription());
        profile.setDistrict(request.getDistrict());
        profile.setProfession(request.getProfession());

        // Parse and set date of birth with error handling
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

    /**
     * Creates Mentor profile with availability status
     * Sets default availability to AVAILABLE
     */
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

    /**
     * Creates NGO profile with verification flag
     * New NGO profiles require manual verification
     */
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

    /**
     * Creates Funder profile
     */
    private UserProfileDTO createFunderProfile(User user, RegistrationRequest request) {
        FunderProfile profile = new FunderProfile(user);
        profile.setFunderName(request.getFunderName());
        profile.setFundingFocus(request.getFundingFocus());

        FunderProfile savedProfile = funderProfileRepository.save(profile);
        log.info("Created funder profile for user: {}", user.getEmail());

        return buildUserProfileDTO(user, savedProfile.getFunderName(), "Funder");
    }

    /**
     * Creates Service Provider profile with verification flag
     * New service providers require manual verification
     */
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

    /**
     * Creates basic profile for admin users
     * Admins don't need detailed profiles
     */
    private UserProfileDTO createBasicUserProfile(User user) {
        log.info("Created basic profile for admin user: {}", user.getEmail());
        return buildUserProfileDTO(user, "Admin", "User");
    }

    /**
     * Helper method to build UserProfileDTO consistently
     */
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

    /**
     * Updates user profile via web interface
     *
     * @param email User's email
     * @param request Profile update request
     * @return Updated YouthProfile entity
     */
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

    /**
     * Updates user profile via USSD interface
     *
     * @param phoneNumber User's phone number
     * @param request Profile update request
     * @return Updated UserProfileDTO
     * @throws UserNotFoundException if user or profile not found
     */
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
    // MENTOR-SPECIFIC METHODS
    // ========================================================================

    /**
     * Retrieves all active mentors with their profiles
     *
     * Used for mentor discovery and matching features.
     * Returns comprehensive mentor information including availability status.
     *
     * @return List of MentorProfileDTO objects
     */
    @Override
    public List<MentorProfileDTO> getAllMentors() {
        List<User> mentorUsers = userRepository.findByRoleAndIsActiveTrue(Role.MENTOR);

        return mentorUsers.stream().map(user -> {
            MentorProfile profile = mentorProfileRepository.findByUser_Id(user.getId())
                    .orElse(new MentorProfile(user));

            return MentorProfileDTO.builder()
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
     * Retrieves mentor by ID
     * FIXED: Added missing method implementation
     *
     * Provides detailed information about a specific mentor including their
     * availability status, expertise, and years of experience. This is used
     * for mentor profile pages and booking systems.
     *
     * @param mentorId Mentor's user ID
     * @return MentorProfileDTO with comprehensive mentor information
     * @throws UserNotFoundException if mentor not found
     * @throws IllegalArgumentException if user is not a mentor
     */
    @Override
    public MentorProfileDTO getMentorById(Long mentorId) {
        log.debug("Retrieving mentor by ID: {}", mentorId);

        User user = getUserById(mentorId);

        // Validate that the user is actually a mentor
        if (user.getRole() != Role.MENTOR) {
            throw new IllegalArgumentException("User with ID " + mentorId + " is not a mentor");
        }

        MentorProfile profile = mentorProfileRepository.findByUser_Id(mentorId)
                .orElseThrow(() -> new UserNotFoundException("Mentor profile not found for user ID: " + mentorId));

        return MentorProfileDTO.builder()
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

    /**
     * Retrieves all active users by role
     *
     * @param role User role to filter by
     * @return List of active users with specified role
     */
    @Override
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleAndIsActiveTrue(role);
    }

    /**
     * Searches active users by search term
     *
     * Searches across multiple fields (email, name, etc.)
     *
     * @param searchTerm Search query string
     * @return List of matching active users
     */
    @Override
    public List<User> searchUsers(String searchTerm) {
        return userRepository.searchActiveUsers(searchTerm);
    }

    /**
     * Gets count of active users by role
     *
     * @param role User role to count
     * @return Number of active users with specified role
     */
    @Override
    public long getUserCountByRole(Role role) {
        return userRepository.countActiveUsersByRole(role);
    }

    /**
     * Checks if email already exists
     *
     * @param email Email to check
     * @return true if email exists
     */
    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Checks if phone number already exists
     *
     * @param phoneNumber Phone number to check
     * @return true if phone number exists
     */
    @Override
    public boolean phoneExists(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    // ========================================================================
    // ACCOUNT MANAGEMENT METHODS
    // ========================================================================

    /**
     * Deactivates user account
     *
     * Soft delete - sets active flag to false but retains all user data.
     * User can be reactivated later if needed.
     *
     * @param userId User ID to deactivate
     */
    @Override
    @Transactional
    public void deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
        log.info("Deactivated user account: {}", user.getEmail());
    }

    /**
     * Reactivates previously deactivated user account
     *
     * @param userId User ID to reactivate
     */
    @Override
    @Transactional
    public void reactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(true);
        userRepository.save(user);
        log.info("Reactivated user account: {}", user.getEmail());
    }
}