package com.youthconnect.notification.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * SMS REQUEST DTO - SMS Notification Request (UUID-based)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Changes from Original:
 * - userId changed from Long → UUID (as per guidelines)
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

    /**
     * Recipient phone number (Uganda format: +256XXXXXXXXX)
     */
    @NotBlank(message = "Recipient phone number is required")
    private String recipient;

    /**
     * SMS message content
     */
    @NotBlank(message = "Message content is required")
    private String message;

    /**
     * Message type (TRANSACTIONAL, PROMOTIONAL)
     */
    private String messageType;

    /**
     * Priority level (1=HIGH, 2=MEDIUM, 3=LOW)
     */
    private Integer priority;

    /**
     * Sender ID (optional)
     */
    private String senderId;

    /**
     * User ID (UUID) - For logging purposes
     */
    private UUID userId;
}