package com.youthconnect.content_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object (DTO) for Learning Module Progress tracking.
 *
 * <p>This class represents a user's progress on a specific learning module,
 * including completion status, playback position, time spent, and quiz scores.</p>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Resume playback from last position (USSD and Web)</li>
 *   <li>Track completion for certificate generation</li>
 *   <li>Calculate total learning time for analytics</li>
 *   <li>Unlock prerequisites and learning paths</li>
 * </ul>
 *
 * @author Douglas Kings Kato
 * @version 1.0
 * @since 2025-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleProgressDTO {

    /**
     * Unique identifier for this progress record.
     */
    private Long progressId;

    /**
     * ID of the user whose progress is being tracked.
     */
    private Long userId;

    /**
     * ID of the learning module.
     */
    private Long moduleId;

    /**
     * Progress percentage (0-100).
     *
     * <p>Calculation:</p>
     * <ul>
     *   <li>Audio modules: Based on playback time vs total duration</li>
     *   <li>Text modules: Based on sections completed</li>
     *   <li>Video modules: Based on watch time</li>
     * </ul>
     *
     * <p>Completion threshold: >= 90%</p>
     */
    private Integer progressPercentage;

    /**
     * Last playback position in seconds.
     * Enables "resume where you left off" functionality.
     *
     * <p>Updated every 30 seconds during playback to minimize API calls.</p>
     */
    private Integer lastPositionSeconds;

    /**
     * Total time spent on this module in seconds.
     * Includes all sessions, even if user rewatches/relistens.
     *
     * <p>Used for:</p>
     * <ul>
     *   <li>User engagement analytics</li>
     *   <li>Module difficulty assessment</li>
     *   <li>Content optimization</li>
     * </ul>
     */
    private Integer timeSpentSeconds;

    /**
     * Flag indicating if module is completed (>= 90% progress).
     * Triggers certificate generation when true.
     */
    private Boolean completed;

    /**
     * Timestamp when module was completed.
     * Null if not yet completed.
     * Used for certificate date.
     */
    private Instant completedAt;

    /**
     * Number of times user has attempted this module.
     * Incremented each time user starts from beginning.
     */
    private Integer attempts;

    /**
     * Quiz score if module has an assessment.
     * Percentage (0-100) or null if no quiz taken.
     *
     * <p>Passing score: >= 70%</p>
     */
    private Double quizScore;

    /**
     * Timestamp when user first started this module.
     */
    private Instant startedAt;

    /**
     * Timestamp of last progress update.
     */
    private Instant updatedAt;

    // ========================================
    // ENRICHED FIELDS
    // ========================================

    /**
     * Module information for context.
     * Includes title, duration, difficulty.
     */
    private LearningModuleBasicInfoDTO module;

    /**
     * Certificate download URL if module is completed.
     * Null if not yet completed or certificate not generated.
     */
    private String certificateUrl;

    // ========================================
    // COMPUTED FIELDS
    // ========================================

    /**
     * Checks if user is eligible for certificate.
     *
     * @return true if completed and quiz score >= 70% (if applicable)
     */
    public Boolean isCertificateEligible() {
        if (!Boolean.TRUE.equals(completed)) {
            return false;
        }
        if (quizScore != null && quizScore < 70.0) {
            return false;
        }
        return true;
    }

    /**
     * Calculates estimated time remaining in minutes.
     *
     * @param moduleDurationMinutes Total module duration
     * @return Estimated minutes remaining, or 0 if completed
     */
    public Integer getEstimatedTimeRemaining(Integer moduleDurationMinutes) {
        if (completed || progressPercentage >= 100) {
            return 0;
        }
        if (moduleDurationMinutes == null || progressPercentage == null) {
            return null;
        }
        int completedMinutes = (moduleDurationMinutes * progressPercentage) / 100;
        return Math.max(0, moduleDurationMinutes - completedMinutes);
    }

    /**
     * Gets formatted time spent as human-readable string.
     *
     * @return Formatted string like "1h 23m" or "45m"
     */
    public String getFormattedTimeSpent() {
        if (timeSpentSeconds == null || timeSpentSeconds == 0) {
            return "0m";
        }
        int minutes = timeSpentSeconds / 60;
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, remainingMinutes);
        }
        return String.format("%dm", minutes);
    }

    // ========================================
    // NESTED CLASS: MODULE BASIC INFO
    // ========================================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LearningModuleBasicInfoDTO {
        private Long moduleId;
        private String title;
        private String category;
        private Integer durationMinutes;
        private String difficultyLevel;
    }
}