package com.youthconnect.mentor_service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ============================================================================
 * SESSION REQUEST DTO (UUID VERSION - FULLY FIXED)
 * ============================================================================
 * Data Transfer Object for mentorship session booking requests.
 * @author Douglas Kings Kato
 * @since 2025-11-07
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequest {

    @NotNull(message = "Mentor ID is required")
    private UUID mentorId;

    @NotNull(message = "Mentee ID is required")
    private UUID menteeId;

    @NotNull(message = "Session date and time is required")
    @Future(message = "Session must be scheduled in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sessionDatetime;

    @NotBlank(message = "Session topic is required")
    @Size(min = 5, max = 255, message = "Topic must be between 5 and 255 characters")
    private String topic;

    @Min(value = 30, message = "Session duration must be at least 30 minutes")
    @Max(value = 240, message = "Session duration cannot exceed 240 minutes (4 hours)")
    @Builder.Default
    private Integer durationMinutes = 60;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private String format; // VIRTUAL, IN_PERSON, PHONE

    public LocalDateTime getSessionEndTime() {
        if (sessionDatetime != null && durationMinutes != null) {
            return sessionDatetime.plusMinutes(durationMinutes);
        }
        return null;
    }

    public boolean isAtLeastHoursInAdvance(int hoursInAdvance) {
        if (sessionDatetime == null) return false;
        LocalDateTime minimumTime = LocalDateTime.now().plusHours(hoursInAdvance);
        return sessionDatetime.isAfter(minimumTime);
    }
}