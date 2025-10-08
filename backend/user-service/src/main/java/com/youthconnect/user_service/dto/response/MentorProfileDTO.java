package com.youthconnect.user_service.dto.response;

import com.youthconnect.user_service.entity.User;
import com.youthconnect.user_service.entity.MentorProfile;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for sending a combined view of a Mentor's User and Profile data.
 */
@Data
@Builder
public class MentorProfileDTO {
    // Include the core user object to get the ID and email
    private User user;
    // Include all the fields from the mentor profile
    private String firstName;
    private String lastName;
    private String bio;
    private String areaOfExpertise;
    private Integer experienceYears;
    private MentorProfile.AvailabilityStatus availabilityStatus;
    // Profile picture for the frontend
    private String profilePicture;
}