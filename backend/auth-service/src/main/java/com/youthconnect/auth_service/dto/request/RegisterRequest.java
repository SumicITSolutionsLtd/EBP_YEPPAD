package com.youthconnect.auth_service.dto.request;

import com.youthconnect.common.validation.ValidEmail;
import com.youthconnect.common.validation.ValidName;
import com.youthconnect.common.validation.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    // ========================================================================
    // REQUIRED FIELDS
    // ========================================================================

    @NotBlank(message = "Email is required")
    @ValidEmail(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain uppercase, lowercase, and number"
    )
    private String password;

    @NotBlank(message = "First name is required")
    @ValidName(minLength = 2, maxLength = 50, message = "Invalid first name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @ValidName(minLength = 2, maxLength = 50, message = "Invalid last name")
    private String lastName;

    @NotBlank(message = "Role is required")
    @Pattern(
            regexp = "^(YOUTH|NGO|MENTOR|FUNDER|SERVICE_PROVIDER|COMPANY|RECRUITER|GOVERNMENT)$",
            message = "Invalid role"
    )
    private String role;

    // ========================================================================
    // OPTIONAL FIELDS
    // ========================================================================

    @ValidPhoneNumber(message = "Invalid Ugandan phone number (+256XXXXXXXXX or 07XXXXXXXX)")
    private String phoneNumber;

    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    @Pattern(
            regexp = "^(MALE|FEMALE|OTHER|PREFER_NOT_TO_SAY)?$",
            message = "Invalid gender"
    )
    private String gender;

    @Size(max = 100, message = "Profession must not exceed 100 characters")
    private String profession;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
