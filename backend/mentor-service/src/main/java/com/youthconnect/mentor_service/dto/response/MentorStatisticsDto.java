package com.youthconnect.mentor_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * ============================================================================
 * MENTOR STATISTICS DTO
 * ============================================================================
 *
 * Response DTO for mentor performance metrics and statistics.
 * Used in mentor dashboards and analytics.
 *
 * @author Douglas Kings Kato
 * @since 2025-11-07
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorStatisticsDto {

    /**
     * Mentor user UUID
     * ✅ FIXED: Changed from Long to UUID
     */
    private UUID mentorId;

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

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Calculate completion rate
     * Completion rate = (completed / total) * 100
     */
    public void calculateCompletionRate() {
        if (totalSessions != null && totalSessions > 0) {
            this.completionRate = (completedSessions.doubleValue() / totalSessions.doubleValue()) * 100;
        } else {
            this.completionRate = 0.0;
        }
    }

    /**
     * Calculate cancellation rate
     *
     * @return Cancellation rate percentage
     */
    public Double getCancellationRate() {
        if (totalSessions != null && totalSessions > 0) {
            return (cancelledSessions.doubleValue() / totalSessions.doubleValue()) * 100;
        }
        return 0.0;
    }

    /**
     * Calculate no-show rate
     *
     * @return No-show rate percentage
     */
    public Double getNoShowRate() {
        if (totalSessions != null && totalSessions > 0) {
            return (noShowSessions.doubleValue() / totalSessions.doubleValue()) * 100;
        }
        return 0.0;
    }

    /**
     * Get completion rate formatted as string
     *
     * @return Formatted completion rate (e.g., "85.5%")
     */
    public String getCompletionRateFormatted() {
        if (completionRate == null) {
            calculateCompletionRate();
        }
        return String.format("%.1f%%", completionRate);
    }

    /**
     * Get average rating formatted as string
     *
     * @return Formatted average rating (e.g., "4.5 / 5.0")
     */
    public String getAverageRatingFormatted() {
        if (averageRating == null) {
            return "No ratings yet";
        }
        return String.format("%.1f / 5.0", averageRating);
    }

    /**
     * Check if mentor has good performance metrics
     * Good performance: completion rate > 80% AND average rating > 4.0
     *
     * @return true if mentor meets performance criteria
     */
    public boolean hasGoodPerformance() {
        if (completionRate == null) {
            calculateCompletionRate();
        }
        return completionRate >= 80.0 &&
                averageRating != null &&
                averageRating >= 4.0;
    }
}