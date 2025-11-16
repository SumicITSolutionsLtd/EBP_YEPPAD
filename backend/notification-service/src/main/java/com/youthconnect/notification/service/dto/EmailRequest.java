package com.youthconnect.notification.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * EMAIL REQUEST DTO - Email Notification Request (UUID-based)
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
public class EmailRequest {

    /**
     * Recipient email address
     */
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String recipient;

    /**
     * Email subject line
     */
    @NotBlank(message = "Subject is required")
    private String subject;

    /**
     * HTML content (optional)
     */
    private String htmlContent;

    /**
     * Plain text content
     */
    private String textContent;

    /**
     * User ID (UUID) - For logging purposes
     */
    private UUID userId;
}