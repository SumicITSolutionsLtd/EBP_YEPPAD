package com.youthconnect.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * DTO for USSD registration confirmation requests.
 * <p>
 * Used by the Notification Service to send personalized SMS confirmations
 * to users who successfully register via USSD (*256#).
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Supports Ugandan phone number validation</li>
 *   <li>Includes user identification and optional confirmation code</li>
 *   <li>Allows dynamic message templates for flexible notifications</li>
 * </ul>
 *
 * <p>Example Usage:</p>
 * <pre>
 * {
 *   "phoneNumber": "+256701430234",
 *   "userName": "Damien",
 *   "userId": 12345,
 *   "confirmationCode": "YCU-56789",
 *   "message": "Dear Damien, your Entrepreneurship Booster Platform account has been successfully created!"
 * }
 * </pre>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UssdConfirmationRequest {

    /**
     * Recipient's phone number in Uganda format.
     * Examples: +256701430234, 0701430234, 256701430234
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(\\+?256|0)[0-9]{9}$",
            message = "Invalid Uganda phone number format"
    )
    private String phoneNumber;

    /**
     * User's first name or display name for personalization.
     */
    @NotBlank(message = "User name is required")
    private String userName;

    /**
     * Unique user ID generated after successful registration.
     */
    private UUID userId;

    /**
     * Optional confirmation code (for verification or tracking).
     * Example: YCU-12345
     */
    private String confirmationCode;

    /**
     * Confirmation or welcome message sent to the user.
     * Can include personalized placeholders.
     */
    @NotBlank(message = "Confirmation message is required")
    private String message;
}
