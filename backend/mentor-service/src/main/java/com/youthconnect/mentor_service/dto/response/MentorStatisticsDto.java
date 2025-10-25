package com.youthconnect.mentor_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * MENTOR STATISTICS DTO
 * ============================================================================
 *
 * Response DTO for mentor performance metrics and statistics.
 * Used in mentor dashboards and analytics.
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
public class MentorStatisticsDto {

    /**
     * Mentor user ID
     */
    private Long mentorId;

    /**
     * Total number of sessions conducted (all statuses)
     */
    private Long totalSessions;

    /**
     * Number of completed sessions
     */
    private Long completedSessions;

    /**
     * Number of cancelled sessions
     */
    private Long cancelledSessions;

    /**
     * Number of no-show sessions
     */
    private Long noShowSessions;

    /**
     * Average rating (1.0 - 5.0)
     */
    private Double averageRating;

    /**
     * Total number of reviews received
     */
    private Long totalReviews;

    /**
     * Session completion rate (percentage)
     */
    private Double completionRate;

    /**
     * Average session duration in minutes
     */
    private Double averageSessionDuration;

    /**
     * Number of active mentees
     */
    private Long activeMentees;

    /**
     * Calculate completion rate
     */
    public void calculateCompletionRate() {
        if (totalSessions != null && totalSessions > 0) {
            this.completionRate = (completedSessions.doubleValue() / totalSessions.doubleValue()) * 100;
        } else {
            this.completionRate = 0.0;
        }
    }
}
