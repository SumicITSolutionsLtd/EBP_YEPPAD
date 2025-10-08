package com.youthconnect.ussd_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ENHANCED: DTO for USSD registration requests to user service
 * This DTO is used by the USSD service to send registration data to the User Service
 * via REST API calls. It matches the expected format of the User Service endpoint.
 *
 * IMPROVEMENTS MADE:
 * - Added comprehensive validation helpers
 * - Enhanced constructor flexibility
 * - Added phone number cleaning utilities
 * - Improved logging and debugging support
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UssdRegistrationRequestDTO {

    /**
     * User's phone number - primary identifier for USSD registrations
     * Should be cleaned and validated before sending to User Service
     */
    private String phoneNumber;

    /**
     * User's first name as collected through USSD flow
     * Required field for registration
     */
    private String firstName;

    /**
     * User's last name as collected through USSD flow
     * Required field for registration
     */
    private String lastName;

    /**
     * User's gender selection from USSD menu
     * Values: "Male", "Female", "Other"
     */
    private String gender;

    /**
     * User's age group selection from USSD menu
     * Values: "18-24", "25-30", "31+"
     */
    private String ageGroup;

    /**
     * User's district/location selection
     * Values: "Madi Okollo", "Zombo", "Nebbi"
     */
    private String district;

    /**
     * User's business development stage
     * Values: "Idea Phase", "Early Stage", "Growth Stage", "Not Applicable"
     */
    private String businessStage;

    /**
     * Constructor with essential fields only
     * Used when only basic information is collected
     */
    public UssdRegistrationRequestDTO(String phoneNumber, String firstName, String lastName, String gender) {
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
    }

    /**
     * ENHANCED: Helper method to validate required fields
     * Ensures all essential data is present before sending to User Service
     */
    public boolean hasRequiredFields() {
        return phoneNumber != null && !phoneNumber.trim().isEmpty() &&
                firstName != null && !firstName.trim().isEmpty() &&
                lastName != null && !lastName.trim().isEmpty();
    }

    /**
     * ENHANCED: Clean phone number for consistent storage
     * Removes formatting characters but preserves the core number
     */
    public String getCleanPhoneNumber() {
        if (phoneNumber == null) {
            return null;
        }

        // Remove all non-digit characters except +
        String cleaned = phoneNumber.replaceAll("[^+0-9]", "");

        // Remove leading + for storage consistency
        if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1);
        }

        return cleaned;
    }

    /**
     * ENHANCED: Get full name for display purposes
     * Combines first and last name with proper spacing
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "Unknown User";
        }

        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";

        return (first + " " + last).trim();
    }

    /**
     * ENHANCED: Validation method for business logic
     * Checks if the DTO contains valid data for registration
     */
    public boolean isValid() {
        // Check required fields
        if (!hasRequiredFields()) {
            return false;
        }

        // Validate phone number format (basic check)
        String clean = getCleanPhoneNumber();
        if (clean == null || clean.length() < 10 || clean.length() > 15) {
            return false;
        }

        // Validate name lengths
        if (firstName.trim().length() < 2 || lastName.trim().length() < 2) {
            return false;
        }

        return true;
    }

    /**
     * ENHANCED: Override toString for better logging
     * Masks sensitive phone number information
     */
    @Override
    public String toString() {
        String maskedPhone = phoneNumber != null && phoneNumber.length() > 6
                ? phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 3)
                : "****";

        return "UssdRegistrationRequestDTO{" +
                "phoneNumber='" + maskedPhone + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender='" + gender + '\'' +
                ", ageGroup='" + ageGroup + '\'' +
                ", district='" + district + '\'' +
                ", businessStage='" + businessStage + '\'' +
                '}';
    }
}