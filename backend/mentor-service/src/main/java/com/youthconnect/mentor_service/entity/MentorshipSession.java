package com.youthconnect.mentor_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * MENTORSHIP SESSION ENTITY
 * ============================================================================
 *
 * Represents a scheduled mentorship session between a mentor and mentee.
 *
 * KEY FEATURES:
 * - Complete session lifecycle tracking (SCHEDULED → COMPLETED/CANCELLED)
 * - Duration tracking for analytics
 * - Separate notes for mentor and mentee
 * - Automatic timestamp management
 *
 * DATABASE TABLE: mentorship_sessions
 *
 * RELATIONSHIPS:
 * - mentor_id → users(user_id) - The mentor conducting the session
 * - mentee_id → users(user_id) - The youth receiving mentorship
 *
 * STATUS FLOW:
 * SCHEDULED → IN_PROGRESS → COMPLETED
 *          ↘ CANCELLED
 *          ↘ NO_SHOW
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Entity
@Table(name = "mentorship_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorshipSession {

    /**
     * Unique session identifier (Primary Key)
     * Auto-generated using MySQL AUTO_INCREMENT strategy
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    /**
     * Mentor user ID (Foreign Key to users table)
     * The professional providing guidance in this session
     *
     * NOT NULL constraint ensures every session has a mentor
     */
    @Column(name = "mentor_id", nullable = false)
    private Long mentorId;

    /**
     * Mentee user ID (Foreign Key to users table)
     * The youth receiving mentorship in this session
     *
     * NOT NULL constraint ensures every session has a mentee
     */
    @Column(name = "mentee_id", nullable = false)
    private Long menteeId;

    /**
     * Scheduled date and time for the session
     * Uses LocalDateTime for easy date/time manipulation
     *
     * IMPORTANT: Always store in UTC, convert to user timezone in presentation layer
     *
     * NOT NULL constraint ensures every session has a scheduled time
     */
    @Column(name = "session_datetime", nullable = false)
    private LocalDateTime sessionDatetime;

    /**
     * Session duration in minutes
     * Default: 60 minutes (1 hour)
     *
     * USAGE:
     * - Used to calculate session end time
     * - Helps with calendar blocking
     * - Analytics on average session length
     */
    @Column(name = "duration_minutes")
    @Builder.Default
    private Integer durationMinutes = 60;

    /**
     * Session topic or focus area
     * Optional field for categorizing sessions
     *
     * EXAMPLES:
     * - "Business Planning"
     * - "Marketing Strategies"
     * - "Financial Management"
     */
    @Column(name = "topic", length = 255)
    private String topic;

    /**
     * Current status of the session
     * Enum values map to database ENUM type
     *
     * STATUS DEFINITIONS:
     * - SCHEDULED: Session is confirmed and upcoming
     * - IN_PROGRESS: Session is currently happening
     * - COMPLETED: Session finished successfully
     * - CANCELLED: Session was cancelled (by either party)
     * - NO_SHOW: Mentee failed to attend scheduled session
     *
     * Default: SCHEDULED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.SCHEDULED;

    /**
     * Private notes written by the mentor
     * Used for tracking discussion points, action items, observations
     *
     * NOT visible to mentee - confidential mentor notes
     *
     * TEXT type in database - supports large content (up to 65KB)
     */
    @Column(name = "mentor_notes", columnDefinition = "TEXT")
    private String mentorNotes;

    /**
     * Notes written by the mentee
     * Used for recording learnings, questions, action items
     *
     * NOT visible to mentor - personal mentee notes
     *
     * TEXT type in database - supports large content (up to 65KB)
     */
    @Column(name = "mentee_notes", columnDefinition = "TEXT")
    private String menteeNotes;

    /**
     * Timestamp when session record was created
     * Automatically set by database on INSERT
     *
     * IMMUTABLE: Never updated after creation
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last update to this record
     * Automatically updated by database on every UPDATE
     *
     * MUTABLE: Changes on every modification
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Session status enumeration
     * Maps to database ENUM type for type safety and performance
     */
    public enum SessionStatus {
        /** Session is confirmed and scheduled for future date */
        SCHEDULED,

        /** Session is currently in progress */
        IN_PROGRESS,

        /** Session completed successfully */
        COMPLETED,

        /** Session was cancelled by mentor or mentee */
        CANCELLED,

        /** Mentee did not attend scheduled session */
        NO_SHOW
    }

    /**
     * JPA lifecycle callback - executed before persisting new entity
     * Sets created_at and updated_at timestamps
     *
     * Called automatically by JPA EntityManager
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now(); // FIXED: Changed from updated_at to updatedAt
    }

    /**
     * JPA lifecycle callback - executed before updating existing entity
     * Updates the updated_at timestamp
     *
     * Called automatically by JPA EntityManager
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Business logic: Calculate session end time
     *
     * @return LocalDateTime representing when the session should end
     */
    public LocalDateTime getSessionEndTime() {
        return sessionDatetime.plusMinutes(durationMinutes);
    }

    /**
     * Business logic: Check if session is in the past
     *
     * @return true if session datetime is before current time
     */
    public boolean isPast() {
        return sessionDatetime.isBefore(LocalDateTime.now());
    }

    /**
     * Business logic: Check if session can be cancelled
     * Only scheduled sessions can be cancelled
     *
     * @return true if status is SCHEDULED
     */
    public boolean canBeCancelled() {
        return status == SessionStatus.SCHEDULED;
    }

    /**
     * Business logic: Check if session can be marked as completed
     * Only in-progress sessions can be marked complete
     *
     * @return true if status is IN_PROGRESS
     */
    public boolean canBeCompleted() {
        return status == SessionStatus.IN_PROGRESS;
    }
}