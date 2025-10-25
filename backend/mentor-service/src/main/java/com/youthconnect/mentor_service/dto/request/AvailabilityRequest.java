package com.youthconnect.mentor_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * ============================================================================
 * AVAILABILITY REQUEST DTO
 * ============================================================================
 *
 * Request DTO for creating/updating mentor availability slots.
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
public class AvailabilityRequest {

    /**
     * Day of week
     */
    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    /**
     * Start time (e.g., "09:00")
     */
    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    /**
     * End time (e.g., "17:00")
     */
    @NotNull(message = "End time is required")
    private LocalTime endTime;

    /**
     * Active status
     */
    private Boolean isActive = true;
}