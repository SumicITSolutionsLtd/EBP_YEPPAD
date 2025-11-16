package com.youthconnect.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * NOTIFICATION RESPONSE DTO - Standardized Response
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Unified response for all notification delivery operations.
 * Replaces raw Map<String, Object> returns to comply with guidelines.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    /**
     * Operation success indicator
     */
    private boolean success;

    /**
     * Delivery status (SENT, PENDING, FAILED, QUEUED)
     */
    private String status;

    /**
     * Provider message ID (for tracking)
     */
    private String messageId;

    /**
     * Masked recipient (phone/email)
     */
    private String recipient;

    /**
     * Error message (if failed)
     */
    private String error;

    /**
     * Will retry indicator (for failures)
     */
    private Boolean willRetry;

    /**
     * Notification log UUID
     */
    private UUID notificationId;

    /**
     * Response timestamp
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Additional metadata
     */
    private Object metadata;
}
