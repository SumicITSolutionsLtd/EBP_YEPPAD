package com.youthconnect.mentor_service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * SESSION REQUEST DTO
 * ============================================================================
 *
 * Data Transfer Object for mentorship session booking requests.
 * Contains all necessary information to schedule a new session.
 *
 * VALIDATION RULES:
 * - Mentor ID: Required, positive number
 * - Mentee ID: Required, positive number (usually from auth context)
 * - Session DateTime: Required, must be in future
 * - Topic: Required, 5-255 characters
 * - Duration: Optional, defaults to 60 minutes, range 30-240 minutes
 *
 * USAGE:
 * - Client sends POST request to book session
 * - Validation runs automatically via @Valid annotation
 * - Service layer performs additional business rule validation
 *
 * EXAMPLE JSON:
 * {
 *   "mentorId": 123,
 *   "menteeId": 456,
 *   "sessionDatetime": "2025-02-15T14:00:00",
 *   "topic": "Business Planning for Agricultural Startup",
 *   "durationMinutes": 90
 * }
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequest {

    /**
     * Mentor user ID
     * The professional who will provide guidance
     */
    @NotNull(message = "Mentor ID is required")
    @Positive(message = "Mentor ID must be a positive number")
    private Long mentorId;

    /**
     * Mentee user ID
     * The youth seeking mentorship
     *
     * NOTE: In production, this should be extracted from JWT token
     * rather than trusting client input
     */
    @NotNull(message = "Mentee ID is required")
    @Positive(message = "Mentee ID must be a positive number")
    private Long menteeId;

    /**
     * Scheduled session date and time
     * Must be in the future
     *
     * Format: ISO-8601 (yyyy-MM-dd'T'HH:mm:ss)
     * Example: 2025-02-15T14:00:00
     */
    @NotNull(message = "Session date and time is required")
    @Future(message = "Session must be scheduled in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sessionDatetime;

    /**
     * Session topic or focus area
     * What will be discussed during the session
     *
     * Examples:
     * - "Business Plan Review"
     * - "Marketing Strategy Guidance"
     * - "Financial Literacy for Entrepreneurs"
     */
    @NotBlank(message = "Session topic is required")
    @Size(min = 5, max = 255, message = "Topic must be between 5 and 255 characters")
    private String topic;

    /**
     * Session duration in minutes
     * Optional - defaults to 60 minutes if not specified
     *
     * Range: 30-240 minutes (0.5 - 4 hours)
     *
     * Common durations:
     * - 30 min: Quick consultation
     * - 60 min: Standard session (default)
     * - 90 min: In-depth discussion
     * - 120 min: Workshop or training
     */
    @Min(value = 30, message = "Session duration must be at least 30 minutes")
    @Max(value = 240, message = "Session duration cannot exceed 240 minutes (4 hours)")
    @Builder.Default
    private Integer durationMinutes = 60; // Default 1 hour

    /**
     * Optional session notes or context
     * Additional information the mentee wants to share
     */
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    /**
     * Preferred session format
     * Virtual (video call), in-person, or phone
     */
    private String format; // VIRTUAL, IN_PERSON, PHONE

    /**
     * Calculate session end time
     *
     * @return LocalDateTime when session ends
     */
    public LocalDateTime getSessionEndTime() {
        if (sessionDatetime != null && durationMinutes != null) {
            return sessionDatetime.plusMinutes(durationMinutes);
        }
        return null;
    }

    /**
     * Check if session is at least X hours in advance
     * Useful for validation rules requiring advance booking
     *
     * @param hoursInAdvance Minimum hours required
     * @return true if session is far enough in advance
     */
    public boolean isAtLeastHoursInAdvance(int hoursInAdvance) {
        if (sessionDatetime == null) return false;
        LocalDateTime minimumTime = LocalDateTime.now().plusHours(hoursInAdvance);
        return sessionDatetime.isAfter(minimumTime);
    }

    /**
     * Get day of week for the session
     *
     * @return DayOfWeek enum value
     */
    public java.time.DayOfWeek getDayOfWeek() {
        return sessionDatetime != null ? sessionDatetime.getDayOfWeek() : null;
    }

    /**
     * Get time of day for the session
     *
     * @return LocalTime
     */
    public java.time.LocalTime getTimeOfDay() {
        return sessionDatetime != null ? sessionDatetime.toLocalTime() : null;
    }
}