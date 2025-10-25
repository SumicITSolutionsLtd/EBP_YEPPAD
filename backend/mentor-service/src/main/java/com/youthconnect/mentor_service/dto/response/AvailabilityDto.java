package com.youthconnect.mentor_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * ============================================================================
 * AVAILABILITY DTO
 * ============================================================================
 *
 * Data Transfer Object for mentor availability data.
 * Represents a mentor's weekly recurring availability schedule.
 *
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
 *   "dayOfWeek": "MONDAY",
 *   "startTime": "09:00:00",
 *   "endTime": "17:00:00",
 *   "isActive": true
 * }
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDto {

    /**
     * Availability record ID (null for new records)
     */
    private Long availabilityId;

    /**
     * Mentor user ID
     */
    private Long mentorId;

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
}