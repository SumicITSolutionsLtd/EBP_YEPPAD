package com.youthconnect.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * USSD Registration Request DTO
 *
 * Used for registering new users via USSD interface.
 * Contains simplified fields suitable for USSD step-by-step input.
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UssdRegisterRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+?256[0-9]{9}$|^0[0-9]{9}$",
            message = "Invalid Ugandan phone number format"
    )
    private String phoneNumber;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String fullName;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;

    @NotBlank(message = "Age group is required")
    private String ageGroup; // "16-20", "21-25", "26-30", "30+"

    @NotBlank(message = "District is required")
    private String district; // Ugandan district

    private String skills; // Comma-separated skills

    @NotBlank(message = "Language preference is required")
    @Pattern(regexp = "en|lg|lur|lgb", message = "Language must be en, lg, lur, or lgb")
    private String languagePreference; // en=English, lg=Luganda, lur=Alur, lgb=Lugbara

    private String sessionId; // USSD session identifier

    /**
     * Default role for USSD registrations is always YOUTH
     */
    public String getRole() {
        return "YOUTH";
    }

    /**
     * Parse full name into first and last name
     * Assumes format: "FirstName LastName" or "FirstName MiddleName LastName"
     */
    public String getFirstName() {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    public String getLastName() {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return parts[parts.length - 1]; // Last word as last name
        }
        return parts[0]; // If only one word, use it as both first and last
    }
}