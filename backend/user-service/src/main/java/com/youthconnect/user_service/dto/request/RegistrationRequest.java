package com.youthconnect.user_service.dto.request;

import com.youthconnect.user_service.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for user registration requests.
 * This class captures all necessary information from the client
 * to register a new user and create their associated profile,
 * accommodating all roles (YOUTH, NGO, FUNDER, SERVICE_PROVIDER, MENTOR).
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Data
public class RegistrationRequest {

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "Role cannot be null")
    private Role role;

    @Pattern(regexp = "^\\+?[0-9. ()-]{10,25}$", message = "Invalid phone number format")
    private String phoneNumber;

    // --- Common fields ---
    private String firstName;
    private String lastName;

    // --- YOUTH profile fields ---
    private String gender;
    private String dateOfBirth;
    private String description;
    private String district; // Added missing field
    private String profession; // Added missing field

    // --- NGO profile fields ---
    private String organisationName;
    private String location;

    // --- FUNDER profile fields ---
    private String funderName;
    private String fundingFocus;

    // --- SERVICE_PROVIDER profile fields ---
    private String providerName;
    private String areaOfExpertise;

    // --- MENTOR profile fields ---
    private String bio;
    private Integer experienceYears;

    /**
     * Validates that required fields are present based on the role
     */
    public boolean hasRequiredFieldsForRole() {
        return switch (role) {
            case YOUTH -> firstName != null && lastName != null;
            case NGO -> organisationName != null && location != null;
            case FUNDER -> funderName != null && fundingFocus != null;
            case SERVICE_PROVIDER -> providerName != null && areaOfExpertise != null && location != null;
            case MENTOR -> firstName != null && lastName != null && areaOfExpertise != null;
            case ADMIN -> true;
        };
    }

    /**
     * Cleans and standardizes phone number format
     */
    public String getCleanPhoneNumber() {
        if (phoneNumber == null) {
            return null;
        }

        String cleaned = phoneNumber.replaceAll("[^+0-9]", "");

        if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1);
        }

        return cleaned;
    }
}