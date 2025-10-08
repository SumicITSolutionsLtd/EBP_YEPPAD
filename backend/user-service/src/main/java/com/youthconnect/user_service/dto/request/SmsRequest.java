package com.youthconnect.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending SMS notification requests.
 * <p>
 * Used by the Notification Service (or Feign client) to send SMS messages
 * such as verification codes, alerts, or general notifications to users.
 * </p>
 *
 * <h3>Validation Rules:</h3>
 * <ul>
 *   <li>Phone number must be a valid Ugandan mobile number (+256 or 0 followed by 9 digits)</li>
 *   <li>Message must not be blank and cannot exceed 500 characters</li>
 * </ul>
 *
 * <p>Example Usage:</p>
 * <pre>
 * {
 *   "phoneNumber": "+256701430234",
 *   "message": "Welcome to Youth Connect Uganda!",
 *   "messageType": "WELCOME",
 *   "userId": 12345
 * }
 * </pre>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

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
     * SMS message content.
     * <p>
     * Each SMS segment is up to 160 characters, but long messages are allowed
     * (they may be split into multiple segments by the SMS gateway).
     * </p>
     */
    @NotBlank(message = "Message content is required")
    @Size(max = 500, message = "Message too long (maximum 500 characters)")
    private String message;

    /**
     * Type or purpose of the message (optional).
     * Examples: WELCOME, VERIFICATION, NOTIFICATION, ALERT.
     */
    private String messageType;

    /**
     * Optional user ID associated with this message (for tracking or auditing).
     */
    private Long userId;
}
