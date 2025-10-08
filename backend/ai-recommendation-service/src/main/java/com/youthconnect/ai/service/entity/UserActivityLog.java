package com.youthconnect.ai.service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity that maps to the existing `user_activity_logs` table.
 *
 * Purpose:
 * - Tracks user activities for analytics and ML model training
 * - Captures context like device, IP, and metadata
 * - Designed to integrate with an existing schema (avoids schema conflicts)
 */
@Entity
@Table(name = "user_activity_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLog {

    /** Primary key - auto-incremented log ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    /** ID of the user performing the activity */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Type of activity (e.g., VIEW_OPPORTUNITY, APPLY_JOB, COMPLETE_MODULE) */
    @Column(name = "activity_type", nullable = false)
    private String activityType;

    /** Target entity’s ID (e.g., Opportunity ID, Mentor ID) */
    @Column(name = "target_id")
    private Long targetId;

    /** Target entity’s type (e.g., OPPORTUNITY, MENTOR, MODULE) */
    @Column(name = "target_type")
    private String targetType;

    /** Session ID to group actions within the same session */
    @Column(name = "session_id")
    private String sessionId;

    /** User agent string (browser, device info) */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /** IP address of the user */
    @Column(name = "ip_address")
    private String ipAddress;

    /** Additional structured metadata in JSON format */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    /** Timestamp when the activity was recorded */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Automatically set createdAt before persisting if not already set.
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
