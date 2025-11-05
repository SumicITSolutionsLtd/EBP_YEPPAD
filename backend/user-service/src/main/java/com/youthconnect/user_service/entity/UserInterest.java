package com.youthconnect.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * UserInterest Entity - User Interest Tracking for AI Recommendations
 *
 * Stores user interests used by the AI recommendation engine to personalize
 * content, opportunities, and mentor suggestions.
 *
 * Interests can be:
 * - USER_SELECTED: Explicitly chosen by the user during registration or profile updates
 * - AI_INFERRED: Automatically detected by analyzing user behavior patterns
 * - ACTIVITY_BASED: Derived from user interactions with platform content
 *
 * Example Interests: "Agriculture", "Technology", "Fintech", "Renewable Energy",
 *                    "Fashion", "Marketing", "Financial Literacy"
 *
 * Database Mapping:
 * - Table: user_interests
 * - Primary Key: user_interest_id (auto-increment)
 * - Unique Constraint: (user_id, interest_tag) - prevents duplicate interests
 * - Foreign Key: user_id references users(user_id)
 *
 * AI Usage:
 * - Collaborative filtering: Find users with similar interests
 * - Content matching: Recommend opportunities aligned with interests
 * - Trend analysis: Identify popular interests across user segments
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2024-01-15
 */
@Entity
@Table(
        name = "user_interests",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_user_interest",
                columnNames = {"user_id", "interest_tag"}
        ),
        indexes = {
                @Index(name = "idx_user_interests", columnList = "user_id, interest_level"),
                @Index(name = "idx_interest_tag", columnList = "interest_tag")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInterest {

    /**
     * Primary key - auto-generated unique identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_interest_id")
    private Long id;

    /**
     * User ID - references the users table
     *
     * NOTE: We use Long instead of @ManyToOne to avoid circular dependencies
     * and improve query performance. The relationship is logical, not physical.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Interest tag/keyword
     *
     * Examples: "Agriculture", "Technology", "Business Development"
     * Case-insensitive comparison recommended when querying
     *
     * Max length: 50 characters
     */
    @Column(name = "interest_tag", nullable = false, length = 50)
    private String interestTag;

    /**
     * Interest level indicating priority for recommendations
     *
     * HIGH: Primary interests (weighted heavily in recommendations)
     * MEDIUM: Secondary interests (moderate weight)
     * LOW: Casual interests (minimal weight)
     *
     * Default: MEDIUM
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "interest_level", nullable = false, length = 10)
    @Builder.Default
    private InterestLevel interestLevel = InterestLevel.MEDIUM;

    /**
     * Interest source indicating how it was discovered
     *
     * USER_SELECTED: User explicitly chose this interest
     * AI_INFERRED: AI detected from user behavior patterns
     * ACTIVITY_BASED: Derived from specific user actions
     *
     * This helps track recommendation accuracy and user preferences
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    @Builder.Default
    private InterestSource source = InterestSource.USER_SELECTED;

    /**
     * Creation timestamp - automatically set on insert
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp - automatically updated on modification
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========================================================================
    // ENUMS
    // ========================================================================

    /**
     * Interest Level Enum
     * Defines the importance/priority of an interest for recommendations
     */
    public enum InterestLevel {
        LOW,      // Casual interest, minimal recommendation weight
        MEDIUM,   // Standard interest, moderate recommendation weight
        HIGH      // Primary interest, heavy recommendation weight
    }

    /**
     * Interest Source Enum
     * Tracks how the interest was added to the user's profile
     */
    public enum InterestSource {
        USER_SELECTED,   // User explicitly selected this interest
        AI_INFERRED,     // AI algorithm inferred from user behavior
        ACTIVITY_BASED   // Derived from specific user activities
    }

    // ========================================================================
    // BUILDER HELPERS
    // ========================================================================

    /**
     * Create user-selected interest with HIGH priority
     * Convenience method for explicit user choices
     *
     * @param userId User's unique identifier
     * @param interestTag Interest keyword
     * @return UserInterest with HIGH level and USER_SELECTED source
     */
    public static UserInterest createUserSelected(Long userId, String interestTag) {
        return UserInterest.builder()
                .userId(userId)
                .interestTag(interestTag)
                .interestLevel(InterestLevel.HIGH)
                .source(InterestSource.USER_SELECTED)
                .build();
    }

    /**
     * Create AI-inferred interest with MEDIUM priority
     * Used when AI detects interests from user behavior
     *
     * @param userId User's unique identifier
     * @param interestTag Interest keyword
     * @return UserInterest with MEDIUM level and AI_INFERRED source
     */
    public static UserInterest createAiInferred(Long userId, String interestTag) {
        return UserInterest.builder()
                .userId(userId)
                .interestTag(interestTag)
                .interestLevel(InterestLevel.MEDIUM)
                .source(InterestSource.AI_INFERRED)
                .build();
    }

    /**
     * Create activity-based interest with LOW priority
     * Used when interest is derived from single user action
     *
     * @param userId User's unique identifier
     * @param interestTag Interest keyword
     * @return UserInterest with LOW level and ACTIVITY_BASED source
     */
    public static UserInterest createActivityBased(Long userId, String interestTag) {
        return UserInterest.builder()
                .userId(userId)
                .interestTag(interestTag)
                .interestLevel(InterestLevel.LOW)
                .source(InterestSource.ACTIVITY_BASED)
                .build();
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Upgrade interest level to next higher priority
     * Used when user engagement increases with interest-related content
     */
    public void upgradeLevel() {
        switch (this.interestLevel) {
            case LOW -> this.interestLevel = InterestLevel.MEDIUM;
            case MEDIUM -> this.interestLevel = InterestLevel.HIGH;
            case HIGH -> {} // Already at highest level
        }
    }

    /**
     * Downgrade interest level to next lower priority
     * Used when user shows decreased engagement
     */
    public void downgradeLevel() {
        switch (this.interestLevel) {
            case HIGH -> this.interestLevel = InterestLevel.MEDIUM;
            case MEDIUM -> this.interestLevel = InterestLevel.LOW;
            case LOW -> {} // Already at lowest level
        }
    }

    /**
     * Check if interest is user-confirmed
     *
     * @return true if user explicitly selected this interest
     */
    public boolean isUserConfirmed() {
        return this.source == InterestSource.USER_SELECTED;
    }

    /**
     * Get numeric weight for recommendation scoring
     *
     * @return Weight value (1.0 for HIGH, 0.5 for MEDIUM, 0.2 for LOW)
     */
    public double getRecommendationWeight() {
        return switch (this.interestLevel) {
            case HIGH -> 1.0;
            case MEDIUM -> 0.5;
            case LOW -> 0.2;
        };
    }

    @Override
    public String toString() {
        return "UserInterest{" +
                "id=" + id +
                ", userId=" + userId +
                ", interestTag='" + interestTag + '\'' +
                ", level=" + interestLevel +
                ", source=" + source +
                '}';
    }
}