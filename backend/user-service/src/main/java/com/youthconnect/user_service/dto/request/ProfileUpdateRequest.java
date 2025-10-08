package com.youthconnect.user_service.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) for receiving profile update requests from the WEB frontend.
 * This class defines the shape of the JSON sent from the React "Edit Profile" form.
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Data
public class ProfileUpdateRequest {

    private String firstName;

    private String lastName;

    @Size(max = 100, message = "Profession must not exceed 100 characters")
    private String profession;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private String district; // Added missing field

    private String gender;

    private String dateOfBirth;

    private String profilePictureUrl;
}