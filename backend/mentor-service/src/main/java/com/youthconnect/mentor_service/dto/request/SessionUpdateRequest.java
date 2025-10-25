package com.youthconnect.mentor_service.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * SESSION UPDATE REQUEST DTO
 * ============================================================================
 *
 * Request DTO for updating existing mentorship sessions.
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
public class SessionUpdateRequest {

    /**
     * New session date and time (optional)
     */
    @FutureOrPresent(message = "Session time must be in the future")
    private LocalDateTime sessionDatetime;

    /**
     * New duration in minutes (optional)
     */
    @Min(value = 30, message = "Session duration must be at least 30 minutes")
    @Max(value = 240, message = "Session duration cannot exceed 240 minutes")
    private Integer durationMinutes;

    /**
     * New topic (optional)
     */
    @Size(min = 5, max = 255, message = "Topic must be between 5 and 255 characters")
    private String topic;

    /**
     * Session notes
     */
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}