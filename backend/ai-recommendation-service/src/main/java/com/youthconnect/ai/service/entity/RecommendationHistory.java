package com.youthconnect.ai.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * Entity for storing recommendation history and effectiveness.
 *
 * Purpose:
 * - Tracks which recommendations were shown to users
 * - Captures interactions (views, clicks, applications, feedback)
 * - Stores algorithm metadata for improving AI models over time
 */
@Entity
@Table(name = "recommendation_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationHistory {

    /** Primary key - auto-generated ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** User who received the recommendation */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Type of recommendation (e.g., OPPORTUNITY, MENTOR, MODULE, CONTENT) */
    @Column(name = "recommendation_type", nullable = false)
    private String recommendationType;

    /** ID of the recommended item */
    @Column(name = "recommended_item_id", nullable = false)
    private Long recommendedItemId;

    /** Confidence score of the recommendation (0.0–1.0) */
    @Column(name = "recommendation_score", nullable = false)
    private Double score;

    /** Version of the algorithm that generated this recommendation */
    @Column(name = "algorithm_version")
    private String algorithmVersion;

    /** Name of the algorithm (e.g., COLLABORATIVE, CONTENT_BASED, HYBRID) */
    @Column(name = "algorithm_name")
    private String algorithmName;

    /** Whether the recommendation was shown/viewed */
    @Column(name = "was_viewed")
    private Boolean wasViewed = false;

    /** Whether the recommendation was clicked */
    @Column(name = "was_clicked")
    private Boolean wasClicked = false;

    /** Whether the user took action (e.g., applied to an opportunity) */
    @Column(name = "was_applied")
    private Boolean wasApplied = false;

    /** Time user spent engaging with the recommended content (in seconds) */
    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    /** Numeric feedback rating from user (1–5) */
    @Column(name = "feedback_rating")
    private Integer feedbackRating;

    /** Optional free-text feedback from user */
    @Column(name = "feedback_comment", columnDefinition = "TEXT")
    private String feedbackComment;

    /** Timestamp when recommendation was created */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Timestamp when user viewed the recommendation */
    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    /** Timestamp when user clicked the recommendation */
    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    /** Timestamp when user applied based on recommendation */
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    /** Timestamp when feedback was given */
    @Column(name = "feedback_at")
    private LocalDateTime feedbackAt;

    /**
     * Automatically set createdAt before persisting if not already set.
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
