package com.youthconnect.mentor_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * MENTOR AVAILABILITY ENTITY
 * ============================================================================
 *
 * Represents mentor's weekly recurring availability schedule.
 * Used for session booking and calendar management.
 *
 * KEY FEATURES:
 * - Weekly recurring schedule (day + time range)
 * - Multiple availability slots per mentor
 * - Active/inactive toggling without deletion
 * - Automatic timestamp tracking
 *
 * DATABASE TABLE: mentor_availability
 *
 * EXAMPLE USE CASE:
 * Mentor available every Monday 9AM-12PM and Wednesday 2PM-5PM
 * Creates 2 availability records with different day_of_week and times
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Entity
@Table(name = "mentor_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorAvailability {

    /**
     * Unique availability record identifier (Primary Key)
     * Auto-generated using MySQL AUTO_INCREMENT strategy
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Long availabilityId;

    /**
     * Mentor user ID (Foreign Key to users table)
     * The mentor whose availability this record represents
     *
     * NOT NULL constraint ensures every availability belongs to a mentor
     * ON DELETE CASCADE: If mentor deleted, all availability deleted
     */
    @Column(name = "mentor_id", nullable = false)
    private Long mentorId;

    /**
     * Day of the week for this availability slot
     * Uses Java DayOfWeek enum (MONDAY through SUNDAY)
     *
     * Stored as VARCHAR in database matching enum name
     *
     * IMPORTANT: Represents RECURRING weekly availability
     * Not specific to a calendar date
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    /**
     * Start time of availability window
     * Uses LocalTime for time-only values (no date component)
     *
     * EXAMPLES:
     * - 09:00:00 (9 AM)
     * - 14:30:00 (2:30 PM)
     *
     * NOT NULL constraint ensures valid time range
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * End time of availability window
     * Uses LocalTime for time-only values (no date component)
     *
     * IMPORTANT: Must be after start_time (enforced by CHECK constraint)
     *
     * EXAMPLES:
     * - 12:00:00 (12 PM noon)
     * - 17:00:00 (5 PM)
     *
     * NOT NULL constraint ensures valid time range
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Active status flag
     * Controls whether this availability slot is currently valid
     *
     * DEFAULT TRUE: New slots are active by default
     *
     * USAGE:
     * - FALSE: Temporarily disable slot without deleting
     * - TRUE: Slot is active and bookable
     *
     * BENEFITS:
     * - Preserve historical data
     * - Easy reactivation
     * - Audit trail maintained
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Timestamp when availability record was created
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
     * JPA lifecycle callback - executed before persisting new entity
     * Sets created_at and updated_at timestamps
     *
     * Called automatically by JPA EntityManager
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
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
     * Business logic: Calculate duration of availability window in minutes
     *
     * @return Integer representing minutes between start and end time
     */
    public int getDurationMinutes() {
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Business logic: Check if a given time falls within this availability
     *
     * @param time The time to check
     * @return true if time is between start_time and end_time (inclusive)
     */
    public boolean isTimeAvailable(LocalTime time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    /**
     * Business logic: Check if availability overlaps with another slot
     * Used to prevent double-booking
     *
     * @param other Another MentorAvailability to check against
     * @return true if there is any time overlap
     */
    public boolean overlapsWith(MentorAvailability other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false; // Different days = no overlap
        }

        // Check if either start or end time falls within the other slot
        return (this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime));
    }

    /**
     * Business logic: Get formatted time range display
     *
     * @return String representation like "09:00 - 12:00"
     */
    public String getTimeRangeDisplay() {
        return String.format("%s - %s", startTime, endTime);
    }
}