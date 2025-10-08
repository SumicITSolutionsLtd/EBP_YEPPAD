package com.youthconnect.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for USSD user registration requests.
 * <p>
 * Used when users register via USSD (*256#) without internet access.
 * </p>
 *
 * <h3>Validation Rules:</h3>
 * <ul>
 *   <li>Phone number must be in valid Uganda format (+256 or 0 followed by 9 digits)</li>
 *   <li>Names must be at least 2 characters and contain only valid name characters</li>
 *   <li>Gender must be one of: MALE, FEMALE, OTHER</li>
 * </ul>
 *
 * <p>Example Usage:</p>
 * <pre>
 * {
 *   "phoneNumber": "+256701430234",
 *   "firstName": "Douglas",
 *   "lastName": "Kato",
 *   "gender": "MALE",
 *   "district": "Kampala",
 *   "ageGroup": "18-24",
 *   "businessStage": "Idea Phase",
 *   "preferredLanguage": "English"
 * }
 * </pre>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UssdRegistrationRequest {

    /**
     * User's phone number in Uganda format.
     * Examples: +256701430234, 0701430234, 256701430234
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(\\+?256|0)[0-9]{9}$",
            message = "Invalid Uganda phone number format"
    )
    private String phoneNumber;

    /**
     * User's first name (supports Ugandan names with special characters).
     */
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z\\s'.-]+$",
            message = "First name contains invalid characters"
    )
    private String firstName;

    /**
     * User's last name (supports Ugandan names with special characters).
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(
            regexp = "^[a-zA-Z\\s'.-]+$",
            message = "Last name contains invalid characters"
    )
    private String lastName;

    /**
     * User's gender.
     * Valid values: MALE, FEMALE, OTHER.
     */
    @NotBlank(message = "Gender is required")
    @Pattern(
            regexp = "^(MALE|FEMALE|OTHER)$",
            message = "Gender must be MALE, FEMALE, or OTHER"
    )
    private String gender;

    /**
     * User's district in Uganda (optional during registration).
     * Examples: Kampala, Nebbi, Madi Okollo, Zombo.
     */
    private String district;

    /**
     * User's age group for statistical purposes.
     * Examples: 18-24, 25-30, 31-35.
     */
    private String ageGroup;

    /**
     * User's business stage (for youth entrepreneurs).
     * Examples: Idea Phase, Early Stage, Growth Stage.
     */
    private String businessStage;

    /**
     * Preferred language for communication.
     * Examples: English, Luganda, Alur, Lugbara.
     */
    private String preferredLanguage;
}
