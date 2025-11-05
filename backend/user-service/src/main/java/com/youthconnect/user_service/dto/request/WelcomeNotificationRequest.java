package com.youthconnect.user_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * DTO for sending welcome notifications to new users.
 * <p>
 * Used by the Notification Service to send personalized welcome messages
 * via SMS, email, or both after successful registration.
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Supports both email and SMS delivery</li>
 *   <li>Includes personalization fields (first name, language, role)</li>
 *   <li>Integrates with NotificationServiceClient</li>
 * </ul>
 *
 * <p>Example Usage:</p>
 * <pre>
 * {
 *   "userId": 12345,
 *   "email": "damienpapers3@gmail.com",
 *   "phoneNumber": "+256701430234",
 *   "firstName": "Damien",
 *   "userRole": "YOUTH",
 *   "preferredLanguage": "en",
 *   "sendEmail": true,
 *   "sendSms": true
 * }
 * </pre>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelcomeNotificationRequest {

    /**
     * Unique ID of the newly registered user.
     */
    @NotNull(message = "User ID is required")
    private UUID userId;

    /**
     * User's email address (optional for SMS-only users).
     */
    @Email(message = "Invalid email format")
    private String email;

    /**
     * User's phone number (optional for email-only users).
     * Examples: +256701430234, 0701430234
     */
    private String phoneNumber;

    /**
     * User's first name used for message personalization.
     */
    @NotBlank(message = "First name is required")
    private String firstName;

    /**
     * User's platform role.
     * Examples: YOUTH, NGO, FUNDER, ADMIN.
     */
    @NotBlank(message = "User role is required")
    private String userRole;

    /**
     * Preferred language for the welcome message.
     * Examples: en (English), lg (Luganda), lur (Alur), lgb (Lugbara).
     */
    private String preferredLanguage;

    /**
     * Whether to send the welcome message via email.
     */
    private boolean sendEmail;

    /**
     * Whether to send the welcome message via SMS.
     */
    private boolean sendSms;
}
