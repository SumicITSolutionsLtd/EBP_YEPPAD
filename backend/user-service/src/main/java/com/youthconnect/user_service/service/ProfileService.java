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
 * Service handling profile-specific operations
 * Separated from UserService for better maintainability
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final YouthProfileRepository youthProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final NgoProfileRepository ngoProfileRepository;
    private final FunderProfileRepository funderProfileRepository;
    private final ServiceProviderProfileRepository serviceProviderProfileRepository;

    /**
     * Update user profile via web interface
     */
    @Transactional
    public YouthProfile updateUserProfile(User user, ProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", user.getEmail());

        YouthProfile profile = youthProfileRepository.findByUser_Id(user.getId())
                .orElse(new YouthProfile(user));

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setProfession(request.getProfession());
        profile.setDescription(request.getDescription());

        return youthProfileRepository.save(profile);
    }

    /**
     * Update user profile via USSD interface
     */
    @Transactional
    public UserProfileDTO updateUserProfileByPhone(User user, ProfileUpdateRequestDTO request) {
        log.info("Updating USSD profile for user: {}", user.getPhoneNumber());

        YouthProfile profile = youthProfileRepository.findByUser_Id(user.getId())
                .orElse(new YouthProfile(user));

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());

        YouthProfile savedProfile = youthProfileRepository.save(profile);

        return UserProfileDTO.builder()
                .userId(user.getId())
                .firstName(savedProfile.getFirstName())
                .lastName(savedProfile.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    /**
     * Get user profile by user ID with role awareness
     */
    public UserProfileDTO getUserProfileById(Long userId, Role role) {
        return switch (role) {
            case YOUTH -> getYouthProfile(userId);
            case MENTOR -> getMentorProfile(userId);
            case NGO -> getNgoProfile(userId);
            case FUNDER -> getFunderProfile(userId);
            case SERVICE_PROVIDER -> getServiceProviderProfile(userId);
            default -> getBasicProfile(userId);
        };
    }

    private UserProfileDTO getYouthProfile(Long userId) {
        return youthProfileRepository.findByUser_Id(userId)
                .map(profile -> UserProfileDTO.builder()
                        .userId(userId)
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .phoneNumber(profile.getUser().getPhoneNumber())
                        .build())
                .orElse(getBasicProfile(userId));
    }

    private UserProfileDTO getMentorProfile(Long userId) {
        return mentorProfileRepository.findByUser_Id(userId)
                .map(profile -> UserProfileDTO.builder()
                        .userId(userId)
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .phoneNumber(profile.getUser().getPhoneNumber())
                        .build())
                .orElse(getBasicProfile(userId));
    }

    // Similar methods for other roles...

    private UserProfileDTO getBasicProfile(Long userId) {
        return UserProfileDTO.builder()
                .userId(userId)
                .firstName("User")
                .lastName(String.valueOf(userId))
                .build();
    }
}