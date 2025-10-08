package com.youthconnect.ussd_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for USSD User Registration Requests.
 *
 * Captures minimal information collected through the USSD flow for youth registration.
 * Due to interface constraints (limited screen size, no typing ease), only essential
 * fields are collected during USSD registration. Additional details can be updated later
 * via the web or mobile interface.
 *
 * Validation Strategy:
 * - Phone number: Primary identifier for USSD users (Uganda format only).
 * - Names: Support African naming conventions (spaces, hyphens, apostrophes, periods).
 * - Optional fields: May be null during initial registration, but encouraged for better
 *   personalization and support matching.
 *
 * Author: Youth Connect Uganda Development Team
 * Version: 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UssdRegistrationRequest {

    // =========================================================================
    // REQUIRED FIELDS
    // =========================================================================

    /**
     * Phone number (Uganda format).
     * Examples: +256700123456, 0700123456, 256700123456.
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(\\+?256|0)[0-9]{9}$",
            message = "Invalid Uganda phone number. Use format: 0700123456 or +256700123456"
    )
    private String phoneNumber;

    /**
     * User's first name.
     * Supports African naming conventions.
     */
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z\\s'.-]+$",
            message = "First name can only contain letters, spaces, apostrophes, periods and hyphens"
    )
    private String firstName;

    /**
     * User's last name / surname.
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z\\s'.-]+$",
            message = "Last name can only contain letters, spaces, apostrophes, periods and hyphens"
    )
    private String lastName;

    // =========================================================================
    // OPTIONAL FIELDS
    // =========================================================================

    /**
     * Gender selection (from USSD menu).
     * Allowed values: Male, Female, Other.
     */
    @Pattern(
            regexp = "^(Male|Female|Other)$",
            message = "Gender must be Male, Female, or Other"
    )
    private String gender;

    /**
     * Age group category for demographic analysis.
     * Allowed values: 18-24, 25-30, 31+.
     */
    @Pattern(
            regexp = "^(18-24|25-30|31\\+)$",
            message = "Age group must be 18-24, 25-30, or 31+"
    )
    private String ageGroup;

    /**
     * District / location in Uganda.
     * Useful for location-based opportunity matching.
     */
    @Size(max = 100, message = "District name cannot exceed 100 characters")
    private String district;

    /**
     * Current business or entrepreneurship stage.
     * Helps in recommending relevant programs.
     */
    @Size(max = 50, message = "Business stage cannot exceed 50 characters")
    private String businessStage;

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    /**
     * Combines first and last name into full name.
     *
     * @return Full name with proper spacing, or null if both are missing.
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return null;
        }
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        return (first + " " + last).trim();
    }

    /**
     * Cleans and normalizes phone number for storage.
     * - Removes spaces, dashes, and parentheses.
     * - Converts local format (070...) → international format (256...).
     * - Removes '+' for consistent storage.
     *
     * @return Cleaned phone number (e.g., "256700123456"), or null if invalid.
     */
    public String getCleanPhoneNumber() {
        if (phoneNumber == null) {
            return null;
        }
        String cleaned = phoneNumber.replaceAll("[^+0-9]", "");
        if (cleaned.startsWith("0")) {
            cleaned = "256" + cleaned.substring(1);
        }
        if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1);
        }
        return cleaned;
    }

    /**
     * Converts phone number into international display format.
     *
     * @return Phone number with + prefix (e.g., "+256700123456").
     */
    public String getInternationalPhoneNumber() {
        String cleaned = getCleanPhoneNumber();
        return cleaned != null ? "+" + cleaned : null;
    }

    /**
     * Validates required registration fields.
     *
     * @return true if phone number, first name, and last name are provided.
     */
    public boolean hasRequiredFields() {
        return phoneNumber != null && !phoneNumber.trim().isEmpty() &&
                firstName != null && !firstName.trim().isEmpty() &&
                lastName != null && !lastName.trim().isEmpty();
    }

    /**
     * Checks if optional demographic fields are provided.
     *
     * @return true if at least one optional field is filled.
     */
    public boolean hasOptionalFields() {
        return (gender != null && !gender.trim().isEmpty()) ||
                (ageGroup != null && !ageGroup.trim().isEmpty()) ||
                (district != null && !district.trim().isEmpty()) ||
                (businessStage != null && !businessStage.trim().isEmpty());
    }

    /**
     * Calculates profile completeness percentage.
     *
     * @return Percentage of fields completed (0–100).
     */
    public int getProfileCompletenessPercentage() {
        int totalFields = 7; // phoneNumber, firstName, lastName, gender, ageGroup, district, businessStage
        int completedFields = 0;
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) completedFields++;
        if (firstName != null && !firstName.trim().isEmpty()) completedFields++;
        if (lastName != null && !lastName.trim().isEmpty()) completedFields++;
        if (gender != null && !gender.trim().isEmpty()) completedFields++;
        if (ageGroup != null && !ageGroup.trim().isEmpty()) completedFields++;
        if (district != null && !district.trim().isEmpty()) completedFields++;
        if (businessStage != null && !businessStage.trim().isEmpty()) completedFields++;
        return (completedFields * 100) / totalFields;
    }

    /**
     * Converts DTO into Auth-Service registration format.
     *
     * @return Map of registration data formatted for auth-service.
     */
    public Map<String, Object> toAuthServiceFormat() {
        Map<String, Object> authData = new HashMap<>();
        authData.put("phoneNumber", getCleanPhoneNumber());
        authData.put("email", generateSyntheticEmail());
        authData.put("firstName", firstName);
        authData.put("lastName", lastName);
        authData.put("role", "YOUTH");
        authData.put("registrationSource", "USSD");

        if (gender != null) authData.put("gender", gender);
        if (ageGroup != null) authData.put("ageGroup", ageGroup);
        if (district != null) authData.put("district", district);
        if (businessStage != null) authData.put("businessStage", businessStage);

        return authData;
    }

    /**
     * Generates synthetic email for USSD users without an email address.
     * Format: ussd_<phoneNumber>@youthconnect.ug
     *
     * @return Synthetic email string.
     */
    private String generateSyntheticEmail() {
        String cleanPhone = getCleanPhoneNumber();
        return "ussd_" + cleanPhone + "@youthconnect.ug";
    }

    /**
     * Privacy-aware toString method.
     * Masks phone number except first 3 and last 3 digits.
     */
    @Override
    public String toString() {
        String maskedPhone = maskPhoneNumber(phoneNumber);
        return "UssdRegistrationRequest{" +
                "phoneNumber='" + maskedPhone + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender='" + gender + '\'' +
                ", ageGroup='" + ageGroup + '\'' +
                ", district='" + district + '\'' +
                ", businessStage='" + businessStage + '\'' +
                ", profileCompleteness=" + getProfileCompletenessPercentage() + "%" +
                '}';
    }

    /**
     * Masks phone number for logging.
     * Example: 070****456 or 256****456
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 6) {
            return "****";
        }
        String cleaned = phone.replaceAll("[^0-9+]", "");
        if (cleaned.length() >= 6) {
            String prefix = cleaned.substring(0, 3);
            String suffix = cleaned.substring(cleaned.length() - 3);
            return prefix + "****" + suffix;
        }
        return "****";
    }
}
