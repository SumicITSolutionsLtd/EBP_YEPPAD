package com.youthconnect.notification.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * WELCOME NOTIFICATION REQUEST DTO (UUID-based)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Changes from Original:
 * - userId changed from Long → UUID
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelcomeNotificationRequest {

    /**
     * User ID (UUID)
     */
    @NotNull(message = "User ID is required")
    private UUID userId;

    /**
     * User email address
     */
    @Email(message = "Invalid email format")
    private String email;

    /**
     * User phone number (Uganda format)
     */
    private String phoneNumber;

    /**
     * User first name
     */
    @NotBlank(message = "First name is required")
    private String firstName;

    /**
     * User role (YOUTH, MENTOR, NGO, etc.)
     */
    @NotBlank(message = "User role is required")
    private String userRole;

    /**
     * Preferred language (en, lg, lur, lgb)
     */
    @Builder.Default
    private String preferredLanguage = "en";
}