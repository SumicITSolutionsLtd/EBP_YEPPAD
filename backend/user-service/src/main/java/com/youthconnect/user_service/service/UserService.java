package com.youthconnect.user_service.service;

import com.youthconnect.user_service.controller.InternalUserController.UserProfileSummary;
import com.youthconnect.user_service.dto.UserProfileDTO;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequest;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequestDTO;
import com.youthconnect.user_service.dto.request.RegistrationRequest;
import com.youthconnect.user_service.dto.request.UssdRegistrationRequest;
import com.youthconnect.user_service.dto.response.ApiResponse;
import com.youthconnect.user_service.dto.response.MentorProfileDTO;
import com.youthconnect.user_service.dto.response.UserProfileResponse;
import com.youthconnect.user_service.entity.Role;
import com.youthconnect.user_service.entity.User;
import com.youthconnect.user_service.entity.YouthProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User createUser(RegistrationRequest request);

    // Corrected signature matching UserServiceImpl
    ApiResponse<Object> createUserProfile(String userId, RegistrationRequest registrationDto);

    User getUserByEmail(String email);
    User getUserByPhone(String phoneNumber);
    User getUserById(UUID userId);

    UserProfileDTO getUserProfileByEmail(String email);
    UserProfileResponse getEnhancedUserProfileByEmail(String email);
    UserProfileDTO getUserProfileByPhone(String phoneNumber);
    UserProfileDTO getUserProfileById(UUID userId);

    UserProfileDTO createUssdUserProfile(User user, UssdRegistrationRequest request);

    // This overrides the public API usage
    UserProfileDTO createUserProfile(User user, RegistrationRequest request);

    YouthProfile updateUserProfile(String email, ProfileUpdateRequest request);
    UserProfileDTO updateUserProfileByPhone(String phoneNumber, ProfileUpdateRequestDTO request);

    List<MentorProfileDTO> getAllMentors();
    Page<MentorProfileDTO> getAllMentors(Pageable pageable);
    MentorProfileDTO getMentorById(UUID mentorId);

    List<User> getUsersByRole(Role role);
    List<User> searchUsers(String searchTerm);
    Page<User> searchUsers(String searchTerm, Pageable pageable);
    long getUserCountByRole(Role role);

    boolean emailExists(String email);
    boolean phoneExists(String phoneNumber);

    // Internal
    boolean userExists(UUID userId);
    UserProfileSummary getUserSummary(UUID userId);
    boolean canUserPostJobs(UUID userId);
    String getUserOrganization(UUID userId);

    void deactivateUser(UUID userId);
    void reactivateUser(UUID userId);
}