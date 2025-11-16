package com.youthconnect.mentor_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ============================================================================
 * SESSION RESPONSE DTO (UUID VERSION - FULLY FIXED)
 * ============================================================================
 * Data Transfer Object for mentorship session response data.
 * @author Douglas Kings Kato
 * @since 2025-11-07
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

    private UUID sessionId;
    private UUID mentorId;
    private String mentorName;
    private UUID menteeId;
    private String menteeName;
    private LocalDateTime sessionDatetime;
    private Integer durationMinutes;
    private String topic;
    private String status;
    private String mentorNotes;
    private String menteeNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean canBeCancelled() {
        return "SCHEDULED".equals(status) &&
                sessionDatetime.isAfter(LocalDateTime.now());
    }

    public boolean isPast() {
        return sessionDatetime.isBefore(LocalDateTime.now());
    }

    public LocalDateTime getSessionEndTime() {
        if (sessionDatetime != null && durationMinutes != null) {
            return sessionDatetime.plusMinutes(durationMinutes);
        }
        return null;
    }
}