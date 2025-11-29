package com.youthconnect.user_service.service.impl;

import com.youthconnect.user_service.client.JobServiceClient;
import com.youthconnect.user_service.client.JobServiceClient.ApplicationSummaryResponse;
import com.youthconnect.user_service.client.JobServiceClient.CurrentEmploymentDto;
import com.youthconnect.user_service.controller.InternalUserController.UserProfileSummary;
import com.youthconnect.user_service.dto.UserProfileDTO;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequest;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequestDTO;
import com.youthconnect.user_service.dto.request.RegistrationRequest;
import com.youthconnect.user_service.dto.request.UssdRegistrationRequest;
import com.youthconnect.user_service.dto.response.ApiResponse;
import com.youthconnect.user_service.dto.response.MentorProfileDTO;
import com.youthconnect.user_service.dto.response.UserProfileResponse;
import com.youthconnect.user_service.entity.*;
import com.youthconnect.user_service.exception.UserAlreadyExistsException;
import com.youthconnect.user_service.exception.UserNotFoundException;
import com.youthconnect.user_service.repository.*;
import com.youthconnect.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final YouthProfileRepository youthProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final FunderProfileRepository funderProfileRepository;
    private final ServiceProviderProfileRepository serviceProviderProfileRepository;

    @Autowired
    private JobServiceClient jobServiceClient;

    @Override
    @Transactional
    public User createUser(RegistrationRequest request) {
        log.info("Creating new user: email={}, role={}", request.getEmail(), request.getRole());

        if (emailExists(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        if (request.getPhoneNumber() != null && phoneExists(request.getCleanPhoneNumber())) {
            throw new UserAlreadyExistsException("Phone number already registered");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .phoneNumber(request.getCleanPhoneNumber())
                .passwordHash(request.getPassword())
                .role(request.getRole())
                .isActive(true)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        createUserProfileForRole(savedUser, request);

        return savedUser;
    }

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
        }
    }

    /**
     * FIXED METHOD: Uses ApiResponse.success() factory method.
     */
    @Override
    @Transactional
    public ApiResponse<Object> createUserProfile(String userId, RegistrationRequest registrationDto) {
        UUID uuid = UUID.fromString(userId);

        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        log.info("Internal API: Creating profile for user: {} role: {}", userId, user.getRole());

        // FIX: Use ApiResponse.success(data, message) instead of new ApiResponse(...)
        if (user.getRole() == Role.YOUTH && youthProfileRepository.findByUser_Id(uuid).isPresent()) {
            log.warn("Profile already exists for user {}. Returning success to ensure idempotency.", userId);
            return ApiResponse.success(userId, "Profile already exists");
        }
        if (user.getRole() == Role.MENTOR && mentorProfileRepository.findByUser_Id(uuid).isPresent()) {
            return ApiResponse.success(userId, "Profile already exists");
        }
        if (user.getRole() == Role.NGO && ngoProfileRepository.findByUser_Id(uuid).isPresent()) {
            return ApiResponse.success(userId, "Profile already exists");
        }

        if (user.getRole() == Role.YOUTH) {
            YouthProfile profile = new YouthProfile();
            profile.setFirstName(registrationDto.getFirstName());
            profile.setLastName(registrationDto.getLastName());
            profile.setGender(registrationDto.getGender());
            profile.setDistrict(registrationDto.getDistrict());
            profile.setDescription(registrationDto.getDescription());
            profile.setUser(user);
            youthProfileRepository.save(profile);
            log.info("Youth Profile created explicitly for user: {}", userId);
        }
        else if (user.getRole() == Role.MENTOR) {
            MentorProfile profile = new MentorProfile();
            profile.setFirstName(registrationDto.getFirstName());
            profile.setLastName(registrationDto.getLastName());
            profile.setBio(registrationDto.getBio());
            profile.setUser(user);
            mentorProfileRepository.save(profile);
        }
        else if (user.getRole() == Role.NGO) {
            NgoProfile profile = new NgoProfile();
            profile.setOrganisationName(registrationDto.getOrganisationName());
            profile.setLocation(registrationDto.getDistrict());
            profile.setUser(user);
            ngoProfileRepository.save(profile);
        }

        // FIX: Use ApiResponse.success(data, message)
        return ApiResponse.success(userId, "Profile created successfully");
    }

    @Override
    public boolean userExists(UUID userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public UserProfileSummary getUserSummary(UUID userId) {
        User user = getUserById(userId);
        String fullName = "User " + userId;
        String organizationName = null;

        switch (user.getRole()) {
            case YOUTH:
                Optional<YouthProfile> yp = youthProfileRepository.findByUser_Id(userId);
                if (yp.isPresent()) fullName = yp.get().getFirstName() + " " + yp.get().getLastName();
                break;
            case MENTOR:
                Optional<MentorProfile> mp = mentorProfileRepository.findByUser_Id(userId);
                if (mp.isPresent()) fullName = mp.get().getFirstName() + " " + mp.get().getLastName();
                break;
            case NGO:
                Optional<NgoProfile> np = ngoProfileRepository.findByUser_Id(userId);
                if (np.isPresent()) {
                    organizationName = np.get().getOrganisationName();
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
                Optional<FunderProfile> fp = funderProfileRepository.findByUser_Id(userId);
                if (fp.isPresent()) {
                    organizationName = fp.get().getFunderName();
                    fullName = organizationName;
                }
                break;
            case SERVICE_PROVIDER:
                Optional<ServiceProviderProfile> sp = serviceProviderProfileRepository.findByUser_Id(userId);
                if (sp.isPresent()) {
                    organizationName = sp.get().getProviderName();
                    fullName = organizationName;
                }
                break;
        }

        return new UserProfileSummary(user.getId(), user.getEmail(), fullName, user.getRole().name(), organizationName, user.isActive(), user.isEmailVerified());
    }

    @Override
    public boolean canUserPostJobs(UUID userId) {
        User user = getUserById(userId);
        return user.getRole() == Role.NGO || user.getRole() == Role.COMPANY ||
                user.getRole() == Role.RECRUITER || user.getRole() == Role.GOVERNMENT;
    }

    @Override
    public String getUserOrganization(UUID userId) {
        User user = getUserById(userId);
        String organization = null;

        switch (user.getRole()) {
            case NGO:
                organization = ngoProfileRepository.findByUser_Id(userId).map(NgoProfile::getOrganisationName).orElse(null);
                break;
            case FUNDER:
                organization = funderProfileRepository.findByUser_Id(userId).map(FunderProfile::getFunderName).orElse(null);
                break;
            case SERVICE_PROVIDER:
                organization = serviceProviderProfileRepository.findByUser_Id(userId).map(ServiceProviderProfile::getProviderName).orElse(null);
                break;
            default:
                organization = "Organization (ID: " + userId + ")";
        }
        return organization != null ? organization : "Organization (ID: " + userId + ")";
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    public User getUserByPhone(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    @Override
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public UserProfileDTO getUserProfileByEmail(String email) {
        log.debug("Retrieving profile with job stats for email: {}", email);
        User user = getUserByEmail(email);
        return getUserProfileById(user.getId());
    }

    public UserProfileResponse getEnhancedUserProfileByEmail(String email) {
        log.debug("Retrieving enhanced profile with job stats for email: {}", email);
        User user = getUserByEmail(email);

        UserProfileResponse profile = buildUserProfileResponse(user);

        try {
            enrichProfileWithJobStatistics(profile, user.getId());
        } catch (Exception e) {
            log.warn("Failed to fetch job statistics for user {}: {}", user.getId(), e.getMessage());
        }

        return profile;
    }

    @Override
    public UserProfileDTO getUserProfileByPhone(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found with phone: " + phoneNumber));

        YouthProfile profile = youthProfileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Profile not found for user: " + user.getEmail()));

        return UserProfileDTO.builder()
                .userId(user.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    @Override
    public UserProfileDTO getUserProfileById(UUID userId) {
        User user = getUserById(userId);

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

        return buildUserProfileDTO(user, "User", String.valueOf(user.getId()));
    }

    private void enrichProfileWithJobStatistics(UserProfileResponse profile, UUID userId) {
        try {
            ApplicationSummaryResponse appSummary = jobServiceClient.getUserApplicationSummary(userId);

            if (appSummary != null) {
                profile.setTotalJobApplications(appSummary.totalApplications());
                profile.setApprovedJobApplications(appSummary.approvedApplications());
                profile.setPendingJobApplications(appSummary.pendingApplications());
                profile.setRejectedJobApplications(appSummary.rejectedApplications());
                profile.setJobApplicationSuccessRate(appSummary.successRate());
            }

            CurrentEmploymentDto employment = jobServiceClient.getCurrentEmployment(userId);

            if (employment != null && employment.isCurrent()) {
                profile.setCurrentEmploymentStatus(
                        String.format("Employed at %s as %s", employment.companyName(), employment.jobTitle())
                );
                UUID jobIdAsUuid = convertToUUID(employment.jobId());

                UserProfileResponse.CurrentJobDetails jobDetails = UserProfileResponse.CurrentJobDetails.builder()
                        .jobId(jobIdAsUuid)
                        .jobTitle(employment.jobTitle())
                        .companyName(employment.companyName())
                        .employmentType(employment.employmentType())
                        .startDate(employment.startDate())
                        .build();

                profile.setCurrentJob(jobDetails);
            } else {
                profile.setCurrentEmploymentStatus("Seeking Opportunities");
            }

        } catch (Exception e) {
            log.error("Error enriching profile with job statistics for user {}: {}", userId, e.getMessage());
            profile.setTotalJobApplications(0);
            profile.setPendingJobApplications(0);
            profile.setApprovedJobApplications(0);
            profile.setCurrentEmploymentStatus("Status unavailable");
        }
    }

    private UUID convertToUUID(Object jobId) {
        if (jobId == null) return null;
        if (jobId instanceof UUID) return (UUID) jobId;
        if (jobId instanceof Long) return new UUID(0L, (Long) jobId);
        return null;
    }

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

        switch (user.getRole()) {
            case YOUTH:
                youthProfileRepository.findByUser_Id(user.getId()).ifPresent(p -> {
                    builder.firstName(p.getFirstName())
                            .lastName(p.getLastName())
                            .gender(p.getGender())
                            .district(p.getDistrict())
                            .profession(p.getProfession())
                            .description(p.getDescription());
                });
                break;
            case MENTOR:
                mentorProfileRepository.findByUser_Id(user.getId()).ifPresent(p -> {
                    builder.firstName(p.getFirstName())
                            .lastName(p.getLastName())
                            .description(p.getBio());
                });
                break;
            case NGO:
                ngoProfileRepository.findByUser_Id(user.getId()).ifPresent(p -> {
                    builder.firstName(p.getOrganisationName())
                            .description(p.getDescription());
                });
                break;
            case FUNDER:
                funderProfileRepository.findByUser_Id(user.getId()).ifPresent(p -> {
                    builder.firstName(p.getFunderName());
                });
                break;
            case SERVICE_PROVIDER:
                serviceProviderProfileRepository.findByUser_Id(user.getId()).ifPresent(p -> {
                    builder.firstName(p.getProviderName())
                            .description(p.getAreaOfExpertise());
                });
                break;
        }

        return builder.build();
    }

    @Override
    @Transactional
    public UserProfileDTO createUssdUserProfile(User user, UssdRegistrationRequest request) {
        log.info("Creating USSD profile for user {}", user.getPhoneNumber());

        Optional<YouthProfile> existing = youthProfileRepository.findByUser_Id(user.getId());
        if (existing.isPresent()) {
            log.info("USSD Profile already exists for user {}", user.getId());
            return buildUserProfileDTO(user, existing.get().getFirstName(), existing.get().getLastName());
        }

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

    private void createYouthProfileInternal(User user, RegistrationRequest request) {
        if (youthProfileRepository.findByUser_Id(user.getId()).isPresent()) {
            log.info("Youth profile already exists for user {}. Skipping creation.", user.getId());
            return;
        }
        YouthProfile profile = new YouthProfile(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setGender(request.getGender());
        profile.setDescription(request.getDescription());
        youthProfileRepository.save(profile);
        log.debug("Youth profile created for user: {}", user.getId());
    }

    private void createMentorProfileInternal(User user, RegistrationRequest request) {
        if (mentorProfileRepository.findByUser_Id(user.getId()).isPresent()) {
            log.info("Mentor profile already exists for user {}. Skipping creation.", user.getId());
            return;
        }
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
        if (ngoProfileRepository.findByUser_Id(user.getId()).isPresent()) {
            log.info("NGO profile already exists for user {}. Skipping creation.", user.getId());
            return;
        }
        NgoProfile profile = new NgoProfile(user);
        profile.setOrganisationName(request.getOrganisationName());
        profile.setLocation(request.getLocation());
        profile.setDescription(request.getDescription());
        ngoProfileRepository.save(profile);
        log.debug("NGO profile created for user: {}", user.getId());
    }

    private void createFunderProfileInternal(User user, RegistrationRequest request) {
        if (funderProfileRepository.findByUser_Id(user.getId()).isPresent()) {
            log.info("Funder profile already exists for user {}. Skipping creation.", user.getId());
            return;
        }
        FunderProfile profile = new FunderProfile(user);
        profile.setFunderName(request.getFunderName());
        profile.setFundingFocus(request.getFundingFocus());
        funderProfileRepository.save(profile);
        log.debug("Funder profile created for user: {}", user.getId());
    }

    private void createServiceProviderProfileInternal(User user, RegistrationRequest request) {
        if (serviceProviderProfileRepository.findByUser_Id(user.getId()).isPresent()) {
            log.info("Service Provider profile already exists for user {}. Skipping creation.", user.getId());
            return;
        }
        ServiceProviderProfile profile = new ServiceProviderProfile(user);
        profile.setProviderName(request.getProviderName());
        profile.setLocation(request.getLocation());
        profile.setAreaOfExpertise(request.getAreaOfExpertise());
        serviceProviderProfileRepository.save(profile);
        log.debug("Service provider profile created for user: {}", user.getId());
    }

    private UserProfileDTO createYouthProfile(User user, RegistrationRequest request) {
        Optional<YouthProfile> existing = youthProfileRepository.findByUser_Id(user.getId());
        if (existing.isPresent()) {
            return buildUserProfileDTO(user, existing.get().getFirstName(), existing.get().getLastName());
        }

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
        Optional<MentorProfile> existing = mentorProfileRepository.findByUser_Id(user.getId());
        if (existing.isPresent()) {
            return buildUserProfileDTO(user, existing.get().getFirstName(), existing.get().getLastName());
        }

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
        Optional<NgoProfile> existing = ngoProfileRepository.findByUser_Id(user.getId());
        if (existing.isPresent()) {
            return buildUserProfileDTO(user, existing.get().getOrganisationName(), "Organization");
        }

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
        Optional<FunderProfile> existing = funderProfileRepository.findByUser_Id(user.getId());
        if (existing.isPresent()) {
            return buildUserProfileDTO(user, existing.get().getFunderName(), "Funder");
        }

        FunderProfile profile = new FunderProfile(user);
        profile.setFunderName(request.getFunderName());
        profile.setFundingFocus(request.getFundingFocus());

        FunderProfile savedProfile = funderProfileRepository.save(profile);
        log.info("Created funder profile for user: {}", user.getEmail());

        return buildUserProfileDTO(user, savedProfile.getFunderName(), "Funder");
    }

    private UserProfileDTO createServiceProviderProfile(User user, RegistrationRequest request) {
        Optional<ServiceProviderProfile> existing = serviceProviderProfileRepository.findByUser_Id(user.getId());
        if (existing.isPresent()) {
            return buildUserProfileDTO(user, existing.get().getProviderName(), "Service Provider");
        }

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

    @Override
    public List<MentorProfileDTO> getAllMentors() {
        log.debug("Fetching all mentors (non-paginated)");
        List<User> mentorUsers = userRepository.findByRoleAndIsActiveTrue(Role.MENTOR);
        return mentorUsers.stream().map(user -> {
            MentorProfile profile = mentorProfileRepository.findByUser_Id(user.getId())
                    .orElseGet(() -> new MentorProfile(user));

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

    @Override
    public Page<MentorProfileDTO> getAllMentors(Pageable pageable) {
        Page<User> mentorUsers = userRepository.findByRoleAndIsActiveTrue(Role.MENTOR, pageable);
        return mentorUsers.map(user -> {
            MentorProfile profile = mentorProfileRepository.findByUser_Id(user.getId())
                    .orElseGet(() -> new MentorProfile(user));

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
        User user = getUserById(mentorId);
        if (user.getRole() != Role.MENTOR) throw new IllegalArgumentException("User is not a mentor");

        MentorProfile profile = mentorProfileRepository.findByUser_Id(mentorId)
                .orElseThrow(() -> new UserNotFoundException("Mentor profile not found"));

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

    @Override
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleAndIsActiveTrue(role);
    }

    @Override
    public List<User> searchUsers(String searchTerm) {
        return userRepository.searchActiveUsers(searchTerm);
    }

    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchActiveUsers(searchTerm, pageable);
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

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void reactivateUser(UUID userId) {
        User user = getUserById(userId);
        user.setActive(true);
        userRepository.save(user);
    }
}