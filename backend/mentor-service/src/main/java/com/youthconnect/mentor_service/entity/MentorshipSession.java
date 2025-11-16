package com.youthconnect.mentor_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ============================================================================
 * MENTORSHIP SESSION ENTITY (UUID VERSION - FULLY FIXED)
 * ============================================================================
 *
 * Represents a scheduled mentorship session between a mentor and mentee.
 *
 * COMPLIANCE WITH BACKEND GUIDELINES:
 * ✅ Uses UUID for all identifiers (session_id, mentor_id, mentee_id)
 * ✅ PostgreSQL compatible
 * ✅ No mixed ID types - all are UUID
 * ✅ Proper database migrations via Flyway
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (Fully UUID Compliant)
 * @since 2025-11-07
 * ============================================================================
 */
@Entity
@Table(name = "mentorship_sessions", indexes = {
        @Index(name = "idx_session_mentor_status_datetime",
                columnList = "mentor_id, status, session_datetime"),
        @Index(name = "idx_session_mentee_status_datetime",
                columnList = "mentee_id, status, session_datetime"),
        @Index(name = "idx_session_datetime",
                columnList = "session_datetime"),
        @Index(name = "idx_session_status",
                columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorshipSession {

    /**
     * Unique session identifier (Primary Key)
     * Uses UUID for distributed system compatibility
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "session_id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID sessionId;

    /**
     * Mentor user ID (Foreign Key to users table in user-service)
     */
    @Column(name = "mentor_id", nullable = false, columnDefinition = "UUID")
    private UUID mentorId;

    /**
     * Mentee user ID (Foreign Key to users table in user-service)
     */
    @Column(name = "mentee_id", nullable = false, columnDefinition = "UUID")
    private UUID menteeId;

    /**
     * Scheduled date and time for the session
     */
    @Column(name = "session_datetime", nullable = false)
    private LocalDateTime sessionDatetime;

    /**
     * Session duration in minutes (default: 60)
     */
    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Integer durationMinutes = 60;

    /**
     * Session topic or focus area
     */
    @Column(name = "topic", length = 255)
    private String topic;

    /**
     * Current status of the session
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.SCHEDULED;

    /**
     * Private notes written by the mentor
     */
    @Column(name = "mentor_notes", columnDefinition = "TEXT")
    private String mentorNotes;

    /**
     * Notes written by the mentee
     */
    @Column(name = "mentee_notes", columnDefinition = "TEXT")
    private String menteeNotes;

    /**
     * Timestamp when session record was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last update
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Session status enumeration
     */
    public enum SessionStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods
    public LocalDateTime getSessionEndTime() {
        if (sessionDatetime != null && durationMinutes != null) {
            return sessionDatetime.plusMinutes(durationMinutes);
        }
        return null;
    }

    public boolean isPast() {
        return sessionDatetime != null && sessionDatetime.isBefore(LocalDateTime.now());
    }

    public boolean canBeCancelled() {
        return status == SessionStatus.SCHEDULED &&
                sessionDatetime != null &&
                sessionDatetime.isAfter(LocalDateTime.now());
    }

    public boolean canBeCompleted() {
        return status == SessionStatus.IN_PROGRESS ||
                (status == SessionStatus.SCHEDULED && isPast());
    }

    public boolean hasValidParticipants() {
        return mentorId != null &&
                menteeId != null &&
                !mentorId.equals(menteeId);
    }
}