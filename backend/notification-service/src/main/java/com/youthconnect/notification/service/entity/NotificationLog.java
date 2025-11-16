package com.youthconnect.notification.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * NOTIFICATION LOG ENTITY - Database Model (PostgreSQL + UUID)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Tracks notification delivery status across all channels (SMS, Email, Push).
 *
 * Changes from Original:
 * - ID changed from BIGSERIAL → UUID (as per guidelines)
 * - Database changed from MySQL → PostgreSQL
 * - Added Flyway migration support
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Entity
@Table(name = "notification_logs", indexes = {
        @Index(name = "idx_user_id_status", columnList = "user_id, status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    /**
     * Primary Key - UUID instead of BIGSERIAL
     * Automatically generated using UUID strategy
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * User ID (foreign key to users table)
     * References user-service users table
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Type of notification (SMS, EMAIL, PUSH)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 20)
    private NotificationType notificationType;

    /**
     * Recipient identifier (phone number or email)
     */
    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    /**
     * Email subject (null for SMS/Push)
     */
    @Column(name = "subject", length = 255)
    private String subject;

    /**
     * Notification content/body
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Delivery status (PENDING, SENT, DELIVERED, FAILED, BOUNCED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    /**
     * Timestamp when notification was sent
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * Timestamp when notification was delivered (if confirmed)
     */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /**
     * Number of retry attempts made
     */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * Maximum number of retries allowed
     */
    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private Integer maxRetries = 3;

    /**
     * Scheduled time for next retry attempt
     */
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    /**
     * Error message if delivery failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Provider used for delivery (AFRICAS_TALKING, SMTP, FIREBASE)
     */
    @Column(name = "provider", length = 50)
    private String provider;

    /**
     * Provider's message ID (for tracking with provider)
     */
    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    /**
     * Timestamp when record was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp when record was last updated
     */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Notification Type Enum
     */
    public enum NotificationType {
        SMS,   // SMS via Africa's Talking
        EMAIL, // Email via SMTP
        PUSH   // Push notification via Firebase
    }

    /**
     * Notification Status Enum
     */
    public enum NotificationStatus {
        PENDING,   // Queued for delivery
        SENT,      // Sent to provider
        DELIVERED, // Confirmed delivered to recipient
        FAILED,    // Delivery failed (will retry if under max_retries)
        BOUNCED    // Permanently failed (invalid recipient)
    }

    /**
     * JPA Callback: Update timestamp before persisting
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA Callback: Update timestamp before updating
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}