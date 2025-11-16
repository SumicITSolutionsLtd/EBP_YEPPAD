package com.youthconnect.mentor_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ============================================================================
 * REVIEW ENTITY (UUID VERSION - FULLY FIXED)
 * ============================================================================
 * Represents a review given by a mentee to a mentor after a session.
  * @author Douglas Kings Kato
 * @since 2025-11-07
 * ============================================================================
 */
@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(name = "idx_reviewee_id", columnList = "reviewee_id"),
                @Index(name = "idx_reviewer_id", columnList = "reviewer_id"),
                @Index(name = "idx_session_id", columnList = "session_id"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_reviewee_rating", columnList = "reviewee_id, rating")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "review_id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID reviewId;

    @Column(name = "reviewer_id", nullable = false, columnDefinition = "UUID")
    private UUID reviewerId;

    @Column(name = "reviewee_id", nullable = false, columnDefinition = "UUID")
    private UUID revieweeId;

    @Column(name = "session_id", columnDefinition = "UUID")
    private UUID sessionId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    @Builder.Default
    private ReviewType reviewType = ReviewType.MENTOR_SESSION;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private Boolean isApproved = true;

    @Column(name = "is_flagged", nullable = false)
    @Builder.Default
    private Boolean isFlagged = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ReviewType {
        MENTOR_SESSION,
        SERVICE_DELIVERY,
        GENERAL
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isApproved == null) isApproved = true;
        if (isFlagged == null) isFlagged = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Compatibility methods for service layer
    public UUID getMentorId() {
        return this.revieweeId;
    }

    public UUID getMenteeId() {
        return this.reviewerId;
    }

    public UUID getId() {
        return this.reviewId;
    }

    public boolean isPositive() {
        return rating != null && rating >= 4;
    }

    public boolean isNegative() {
        return rating != null && rating <= 2;
    }

    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }
}