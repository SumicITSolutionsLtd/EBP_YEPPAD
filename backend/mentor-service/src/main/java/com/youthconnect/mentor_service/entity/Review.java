package com.youthconnect.mentor_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * REVIEW ENTITY (Refined)
 * ============================================================================
 * Represents a review given by a mentee to a mentor after a mentorship session.
 * Compatible with ReviewService expectations.
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

    /** Primary key - unique identifier for the review */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    /** Reviewer user ID (the mentee providing the review) */
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    /** Reviewee user ID (the mentor being reviewed) */
    @Column(name = "reviewee_id", nullable = false)
    private Long revieweeId;

    /** Associated mentorship session (optional) */
    @Column(name = "session_id")
    private Long sessionId;

    /** Numeric rating on a 1–5 scale */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /** Optional written comment */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    /** Review type classification (default = MENTOR_SESSION) */
    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    @Builder.Default
    private ReviewType reviewType = ReviewType.MENTOR_SESSION;

    /** Whether the review has been approved for public visibility */
    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private Boolean isApproved = true;

    /** Whether the review is flagged for moderation */
    @Column(name = "is_flagged", nullable = false)
    @Builder.Default
    private Boolean isFlagged = false;

    /** Timestamp when the review was created */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the review was last updated */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // -------------------------------------------------------------------------
    // ENUMERATIONS
    // -------------------------------------------------------------------------

    public enum ReviewType {
        MENTOR_SESSION,
        SERVICE_DELIVERY,
        GENERAL
    }

    // -------------------------------------------------------------------------
    // LIFECYCLE CALLBACKS
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // COMPATIBILITY METHODS FOR ReviewService
    // -------------------------------------------------------------------------

    /** Alias for ReviewService compatibility — maps to reviewee (mentor) */
    public Long getMentorId() {
        return this.revieweeId;
    }

    /** Alias for ReviewService compatibility — maps to reviewer (mentee) */
    public Long getMenteeId() {
        return this.reviewerId;
    }

    /** Alias for ReviewService compatibility — maps to reviewId */
    public Long getId() {
        return this.reviewId;
    }

    // -------------------------------------------------------------------------
    // BUSINESS LOGIC METHODS
    // -------------------------------------------------------------------------

    /** Returns true if rating is positive (≥ 4 stars) */
    public boolean isPositive() {
        return rating != null && rating >= 4;
    }

    /** Returns true if rating is negative (≤ 2 stars) */
    public boolean isNegative() {
        return rating != null && rating <= 2;
    }

    /** Returns true if comment is provided */
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }

    /** Returns a visual star representation (e.g., "★★★★☆") */
    public String getStarRepresentation() {
        StringBuilder stars = new StringBuilder();
        int r = rating != null ? rating : 0;
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= r ? "★" : "☆");
        }
        return stars.toString();
    }
}
