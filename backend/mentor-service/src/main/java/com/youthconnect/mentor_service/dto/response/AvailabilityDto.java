package com.youthconnect.mentor_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * ============================================================================
 * AVAILABILITY DTO (FIXED
 * ============================================================================
 *
 * Data Transfer Object for mentor availability data.
 * Represents a mentor's weekly recurring availability schedule.

 * USAGE:
 * - Create mentor availability slots
 * - Update availability schedules
 * - Query available time slots
 *
 * VALIDATION:
 * - dayOfWeek: Required, must be valid day (MONDAY-SUNDAY)
 * - startTime: Required, must be before endTime
 * - endTime: Required, must be after startTime
 * - isActive: Optional, defaults to true
 *
 * EXAMPLE:
 * {
 *   "availabilityId": "550e8400-e29b-41d4-a716-446655440000",
 *   "mentorId": "123e4567-e89b-12d3-a456-426614174000",
 *   "dayOfWeek": "MONDAY",
 *   "startTime": "09:00:00",
 *   "endTime": "17:00:00",
 *   "isActive": true
 * }
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Support)
 * @since 2025-11-07
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDto {

    /**
     * Availability record UUID (null for new records)
     * ✅ FIXED: Changed from Long to UUID
     */
    private UUID availabilityId;

    /**
     * Mentor user UUID
     * ✅ FIXED: Changed from Long to UUID
     */
    private UUID mentorId;

    /**
     * Day of week (MONDAY, TUESDAY, etc.)
     */
    private DayOfWeek dayOfWeek;

    /**
     * Start time (e.g., "09:00:00")
     */
    private LocalTime startTime;

    /**
     * End time (e.g., "17:00:00")
     */
    private LocalTime endTime;

    /**
     * Active status flag
     */
    private Boolean isActive;

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Calculate duration in minutes
     */
    public int getDurationMinutes() {
        if (startTime != null && endTime != null) {
            return (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    /**
     * Get formatted time range display
     */
    public String getTimeRangeDisplay() {
        if (startTime != null && endTime != null) {
            return String.format("%s - %s", startTime, endTime);
        }
        return "";
    }

    /**
     * Check if time falls within this availability slot
     *
     * @param time Time to check
     * @return true if time is within start and end time
     */
    public boolean isTimeAvailable(LocalTime time) {
        if (time == null || startTime == null || endTime == null || !Boolean.TRUE.equals(isActive)) {
            return false;
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
}