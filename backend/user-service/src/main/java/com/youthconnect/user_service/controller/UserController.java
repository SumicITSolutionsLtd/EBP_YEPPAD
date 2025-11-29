package com.youthconnect.user_service.controller;

import com.youthconnect.user_service.dto.request.ProfileUpdateRequest;
import com.youthconnect.user_service.dto.request.ProfileUpdateRequestDTO;
import com.youthconnect.user_service.dto.response.ApiResponse;
import com.youthconnect.user_service.dto.response.MentorProfileDTO;
import com.youthconnect.user_service.dto.response.UserProfileResponse;
import com.youthconnect.user_service.dto.UserProfileDTO;
import com.youthconnect.user_service.entity.YouthProfile;
import com.youthconnect.user_service.service.UserService;
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
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Comprehensive user profile management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    // Only inject the interface
    private final UserService userService;

    @GetMapping("/profile/enhanced")
    @Operation(summary = "Get enhanced user profile with job statistics")
    public ResponseEntity<UserProfileResponse> getEnhancedProfile(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("📊 Fetching enhanced profile for user: {}", maskEmail(userDetails.getUsername()));

        // Uses Interface method
        UserProfileResponse profile = userService.getEnhancedUserProfileByEmail(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}/profile/enhanced")
    @Operation(summary = "Get enhanced profile by user ID (Admin)")
    public ResponseEntity<UserProfileResponse> getEnhancedProfileById(@PathVariable UUID userId) {
        log.info("📊 Fetching enhanced profile for userId: {}", userId);

        var user = userService.getUserById(userId);
        // Uses Interface method
        UserProfileResponse profile = userService.getEnhancedUserProfileByEmail(user.getEmail());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/stats/job-applications")
    @Operation(summary = "Get job application statistics only")
    public ResponseEntity<Map<String, Object>> getJobApplicationStats(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("📈 Fetching job stats for user: {}", maskEmail(userDetails.getUsername()));

        // Uses Interface method
        UserProfileResponse profile = userService.getEnhancedUserProfileByEmail(userDetails.getUsername());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplications", profile.getTotalJobApplications() != null ? profile.getTotalJobApplications() : 0);
        stats.put("pendingApplications", profile.getPendingJobApplications() != null ? profile.getPendingJobApplications() : 0);
        stats.put("approvedApplications", profile.getApprovedJobApplications() != null ? profile.getApprovedJobApplications() : 0);
        stats.put("rejectedApplications", profile.getRejectedJobApplications() != null ? profile.getRejectedJobApplications() : 0);
        stats.put("successRate", profile.getJobApplicationSuccessRate() != null ? profile.getJobApplicationSuccessRate() : 0.0);
        stats.put("currentEmploymentStatus", profile.getCurrentEmploymentStatus() != null ? profile.getCurrentEmploymentStatus() : "Seeking Opportunities");
        stats.put("isEmployed", profile.isEmployed());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's basic profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUserProfile() {
        String userEmail = getCurrentUserEmail();
        UserProfileDTO profile = userService.getUserProfileByEmail(userEmail);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get basic user profile (Legacy)")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getBasicProfile(@AuthenticationPrincipal UserDetails userDetails) {
        var profile = userService.getUserProfileByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    @PutMapping("/me/profile")
    @Operation(summary = "Update current user's profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateCurrentUserProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        String userEmail = getCurrentUserEmail();

        // Service returns Entity, convert to DTO here
        YouthProfile youthProfile = userService.updateUserProfile(userEmail, request);

        UserProfileDTO profileDTO = UserProfileDTO.builder()
                .userId(youthProfile.getUser().getId())
                .firstName(youthProfile.getFirstName())
                .lastName(youthProfile.getLastName())
                .phoneNumber(youthProfile.getUser().getPhoneNumber())
                .email(youthProfile.getUser().getEmail())
                .profession(youthProfile.getProfession())
                .district(youthProfile.getDistrict())
                .description(youthProfile.getDescription())
                .build();

        return ResponseEntity.ok(ApiResponse.success(profileDTO, "Profile updated successfully"));
    }

    @GetMapping("/profile/phone/{phoneNumber}")
    @Operation(summary = "Get user profile by phone number (USSD)")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getUserProfileByPhone(@PathVariable String phoneNumber) {
        UserProfileDTO profile = userService.getUserProfileByPhone(phoneNumber);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    @PutMapping("/profile/phone/{phoneNumber}")
    @Operation(summary = "Update user profile by phone number (USSD)")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateUserProfileByPhone(
            @PathVariable String phoneNumber,
            @Valid @RequestBody ProfileUpdateRequestDTO request) {

        UserProfileDTO updatedProfile = userService.updateUserProfileByPhone(phoneNumber, request);
        return ResponseEntity.ok(ApiResponse.success(updatedProfile, "Profile updated successfully"));
    }

    @GetMapping("/mentors")
    @Operation(summary = "Get all mentors (paginated)")
    public ResponseEntity<Page<MentorProfileDTO>> getAllMentors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Pageable pageable = PageRequest.of(page, size,
                sortDirection.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        return ResponseEntity.ok(userService.getAllMentors(pageable));
    }

    @GetMapping("/mentors/{mentorId}")
    @Operation(summary = "Get mentor by ID")
    public ResponseEntity<ApiResponse<MentorProfileDTO>> getMentorById(@PathVariable UUID mentorId) {
        MentorProfileDTO mentor = userService.getMentorById(mentorId);
        return ResponseEntity.ok(ApiResponse.success(mentor, "Mentor retrieved successfully"));
    }

    // Helper Methods
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new RuntimeException("No authenticated user found in security context");
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        return parts[0].substring(0, Math.min(2, parts[0].length())) + "***@" + parts[1];
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 6) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }
}