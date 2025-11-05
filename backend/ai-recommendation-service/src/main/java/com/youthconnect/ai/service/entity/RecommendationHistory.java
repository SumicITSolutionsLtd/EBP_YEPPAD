package com.youthconnect.ai.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing recommendation history and effectiveness tracking
 * Tracks which recommendations were shown to users and their interactions
 *
 * Purpose:
 * - ML model training and improvement
 * - A/B testing different algorithms
 * - Measuring recommendation quality
 * - User engagement analytics
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Entity
@Table(name = "recommendation_history", indexes = {
        @Index(name = "idx_user_type", columnList = "user_id, recommendation_type"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_was_clicked", columnList = "was_clicked, was_applied")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationHistory {

    /**
     * Primary key - auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who received the recommendation
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Type of recommendation
     * Values: OPPORTUNITY, MENTOR, CONTENT, JOB
     */
    @Column(name = "recommendation_type", nullable = false, length = 50)
    private String recommendationType;

    /**
     * ID of the recommended item
     */
    @Column(name = "recommended_item_id", nullable = false)
    private Long recommendedItemId;

    /**
     * AI confidence score (0.0–1.0)
     * Higher scores indicate stronger recommendations
     */
    @Column(name = "recommendation_score", nullable = false)
    private Double score;

    /**
     * Version of the algorithm that generated this recommendation
     * Used for A/B testing and model comparison
     */
    @Column(name = "algorithm_version", length = 50)
    private String algorithmVersion;

    /**
     * Name of the algorithm used
     * Examples: COLLABORATIVE_FILTERING, CONTENT_BASED, HYBRID
     */
    @Column(name = "algorithm_name", length = 50)
    private String algorithmName;

    /**
     * Whether the recommendation was shown/viewed by user
     */
    @Column(name = "was_viewed")
    @Builder.Default
    private Boolean wasViewed = false;

    /**
     * Whether the recommendation was clicked by user
     */
    @Column(name = "was_clicked")
    @Builder.Default
    private Boolean wasClicked = false;

    /**
     * Whether the user took action (e.g., applied to opportunity)
     */
    @Column(name = "was_applied")
    @Builder.Default
    private Boolean wasApplied = false;

    /**
     * Time user spent engaging with the recommended content (seconds)
     */
    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    /**
     * User feedback rating (1–5 stars)
     */
    @Column(name = "feedback_rating")
    private Integer feedbackRating;

    /**
     * Optional free-text feedback from user
     */
    @Column(name = "feedback_comment", columnDefinition = "TEXT")
    private String feedbackComment;

    /**
     * Timestamp when recommendation was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when user viewed the recommendation
     */
    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    /**
     * Timestamp when user clicked the recommendation
     */
    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    /**
     * Timestamp when user applied based on recommendation
     */
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    /**
     * Timestamp when feedback was given
     */
    @Column(name = "feedback_at")
    private LocalDateTime feedbackAt;

    /**
     * Automatically set createdAt before persisting
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}