package com.youthconnect.user_service.dto.request;

import com.youthconnect.common.validation.ValidEmail;
import com.youthconnect.common.validation.ValidPhoneNumber;
import com.youthconnect.user_service.entity.Role; // Keep local Role enum
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationRequest {

    @NotBlank(message = "Email cannot be empty")
    @ValidEmail(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "Role cannot be null")
    private Role role;

    @ValidPhoneNumber(message = "Invalid phone number format")
    private String phoneNumber;

    // Common fields
    private String firstName;
    private String lastName;

    // YOUTH profile fields
    private String gender;
    private String dateOfBirth;
    private String description;
    private String district;
    private String profession;

    // NGO profile fields
    private String organisationName;
    private String location;

    // FUNDER profile fields
    private String funderName;
    private String fundingFocus;

    // SERVICE_PROVIDER profile fields
    private String providerName;
    private String areaOfExpertise;

    // MENTOR profile fields
    private String bio;
    private Integer experienceYears;

    /**
     * Validates required fields based on role.
     * Ensures data integrity before saving to DB.
     */
    public boolean hasRequiredFieldsForRole() {
        if (role == null) return false;

        return switch (role) {
            case YOUTH -> isPresent(firstName) && isPresent(lastName);
            case NGO -> isPresent(organisationName) && isPresent(location);
            case FUNDER -> isPresent(funderName) && isPresent(fundingFocus);
            case SERVICE_PROVIDER -> isPresent(providerName) && isPresent(areaOfExpertise) && isPresent(location);
            case MENTOR -> isPresent(firstName) && isPresent(lastName) && isPresent(areaOfExpertise);
            case ADMIN, COMPANY, RECRUITER, GOVERNMENT -> true;
        };
    }

    private boolean isPresent(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Cleans and standardizes phone number format
     */
    public String getCleanPhoneNumber() {
        if (phoneNumber == null) return null;
        // Basic cleanup, though Common Lib handles validation
        String cleaned = phoneNumber.replaceAll("[^+0-9]", "");
        if (cleaned.startsWith("+")) cleaned = cleaned.substring(1);
        return cleaned;
    }
}