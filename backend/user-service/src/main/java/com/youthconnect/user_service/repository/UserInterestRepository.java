package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UserInterestRepository - User Interest Data Access Layer
 *
 * Manages user interests for AI-powered personalized recommendations.
 * User interests are used by the AI recommendation engine to suggest
 * relevant opportunities, content, and mentors.
 *
 * Interest Sources:
 * - USER_SELECTED: Explicitly chosen by user during registration or profile update
 * - AI_INFERRED: Automatically detected from user behavior patterns
 * - ACTIVITY_BASED: Derived from user interactions with content
 *
 * Database Table: user_interests
 * Primary Key: user_interest_id (UUID - auto-generated)
 * Unique Constraint: (user_id, interest_tag)
 *
 * FIXED: Changed JpaRepository generic type from Long to UUID
 * to match the entity's primary key type (UUID id).
 *
 * @author Douglas Kings Kato
 * @version 1.1.0 - Fixed UUID type consistency
 */
@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, UUID> {
    // FIXED: Changed from JpaRepository<UserInterest, Long> to JpaRepository<UserInterest, UUID>
    // The second generic parameter must match the entity's @Id field type

    // ========================================================================
    // BASIC QUERY METHODS
    // ========================================================================

    /**
     * Find all interests for a specific user
     * Ordered by interest level (HIGH → MEDIUM → LOW)
     *
     * Use Case: Display user's interest profile on dashboard
     *
     * @param userId User's unique identifier (UUID)
     * @return List of user interests ordered by importance
     */
    @Query("SELECT ui FROM UserInterest ui WHERE ui.userId = :userId " +
            "ORDER BY ui.interestLevel DESC, ui.createdAt DESC")
    List<UserInterest> findByUserId(@Param("userId") UUID userId);

    /**
     * Find a specific interest by user and tag
     * Used to check if interest already exists before adding
     *
     * @param userId User's unique identifier (UUID)
     * @param interestTag Interest keyword (e.g., "Agriculture", "Technology")
     * @return Optional containing the interest if found
     */
    Optional<UserInterest> findByUserIdAndInterestTag(UUID userId, String interestTag);

    /**
     * Check if a user has a specific interest
     * Useful for quick validation without fetching the entity
     *
     * @param userId User's unique identifier (UUID)
     * @param interestTag Interest keyword
     * @return true if user has this interest
     */
    boolean existsByUserIdAndInterestTag(UUID userId, String interestTag);

    // ========================================================================
    // INTEREST LEVEL QUERIES
    // ========================================================================

    /**
     * Find all high-priority interests for a user
     * High interests are weighted more heavily in recommendations
     *
     * Use Case: AI recommendation engine scoring
     *
     * @param userId User's unique identifier (UUID)
     * @return List of HIGH-level interests
     */
    @Query("SELECT ui FROM UserInterest ui WHERE ui.userId = :userId " +
            "AND ui.interestLevel = 'HIGH'")
    List<UserInterest> findHighPriorityInterests(@Param("userId") UUID userId);

    /**
     * Find interests by source type
     * Useful for analyzing how interests were discovered
     *
     * @param userId User's unique identifier (UUID)
     * @param source Interest source (USER_SELECTED, AI_INFERRED, ACTIVITY_BASED)
     * @return List of interests from specified source
     */
    @Query("SELECT ui FROM UserInterest ui WHERE ui.userId = :userId AND ui.source = :source")
    List<UserInterest> findByUserIdAndSource(@Param("userId") UUID userId,
                                             @Param("source") String source);

    // ========================================================================
    // BATCH OPERATIONS
    // ========================================================================

    /**
     * Delete all interests for a user
     * Used when user wants to reset their interest profile
     *
     * WARNING: This is a destructive operation
     *
     * @param userId User's unique identifier (UUID)
     * @return Number of records deleted
     */
    @Modifying
    @Query("DELETE FROM UserInterest ui WHERE ui.userId = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);

    /**
     * Delete a specific interest
     * Used when user explicitly removes an interest
     *
     * @param userId User's unique identifier (UUID)
     * @param interestTag Interest keyword to remove
     * @return Number of records deleted (0 or 1)
     */
    @Modifying
    @Query("DELETE FROM UserInterest ui WHERE ui.userId = :userId " +
            "AND ui.interestTag = :interestTag")
    int deleteByUserIdAndInterestTag(@Param("userId") UUID userId,
                                     @Param("interestTag") String interestTag);

    // ========================================================================
    // ANALYTICS QUERIES
    // ========================================================================

    /**
     * Count total interests for a user
     * Used for profile completeness calculation
     *
     * @param userId User's unique identifier (UUID)
     * @return Number of interests registered
     */
    long countByUserId(UUID userId);

    /**
     * Find users with similar interests (for collaborative filtering)
     * Returns user IDs who share at least one interest
     *
     * Use Case: AI recommendation - find similar users for collaborative filtering
     *
     * FIXED: Changed return type from List<Long> to List<UUID>
     * to match the userId field type in UserInterest entity
     *
     * @param interestTags List of interest keywords
     * @return List of user UUIDs with matching interests
     */
    @Query("SELECT DISTINCT ui.userId FROM UserInterest ui " +
            "WHERE ui.interestTag IN :interestTags")
    List<UUID> findUsersByInterests(@Param("interestTags") List<String> interestTags);

    /**
     * Find most popular interests across all users
     * Used for trending topics and general recommendations
     *
     * Returns interests ordered by frequency (most common first)
     *
     * @param limit Maximum number of results
     * @return List of popular interest tags with counts [interest_tag, count]
     */
    @Query(value = "SELECT interest_tag, COUNT(*) as count " +
            "FROM user_interests " +
            "GROUP BY interest_tag " +
            "ORDER BY count DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findMostPopularInterests(@Param("limit") int limit);

    /**
     * Find interests that are frequently paired together
     * Useful for suggesting related interests to users
     *
     * @param userId User's unique identifier (UUID)
     * @param currentInterestTag User's existing interest
     * @param limit Maximum suggestions
     * @return List of related interest tags with co-occurrence counts
     */
    @Query(value = "SELECT ui2.interest_tag, COUNT(*) as co_occurrence " +
            "FROM user_interests ui1 " +
            "JOIN user_interests ui2 ON ui1.user_id = ui2.user_id " +
            "WHERE ui1.interest_tag = :currentInterestTag " +
            "AND ui2.interest_tag != :currentInterestTag " +
            "AND ui2.user_id != :userId " +
            "GROUP BY ui2.interest_tag " +
            "ORDER BY co_occurrence DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findRelatedInterests(@Param("userId") UUID userId,
                                        @Param("currentInterestTag") String currentInterestTag,
                                        @Param("limit") int limit);

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Update interest level for a specific user interest
     * Used when user engagement patterns indicate changed interest level
     *
     * @param userId User's unique identifier (UUID)
     * @param interestTag Interest keyword
     * @param newLevel New interest level (LOW, MEDIUM, HIGH)
     * @return Number of records updated (should be 1)
     */
    @Modifying
    @Query("UPDATE UserInterest ui SET ui.interestLevel = :newLevel, " +
            "ui.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE ui.userId = :userId AND ui.interestTag = :interestTag")
    int updateInterestLevel(@Param("userId") UUID userId,
                            @Param("interestTag") String interestTag,
                            @Param("newLevel") String newLevel);

    /**
     * Upgrade AI-inferred interests to user-confirmed
     * When user explicitly confirms an AI-suggested interest
     *
     * @param userId User's unique identifier (UUID)
     * @param interestTag Interest keyword
     * @return Number of records updated
     */
    @Modifying
    @Query("UPDATE UserInterest ui SET ui.source = 'USER_SELECTED', " +
            "ui.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE ui.userId = :userId AND ui.interestTag = :interestTag " +
            "AND ui.source = 'AI_INFERRED'")
    int confirmAiInferredInterest(@Param("userId") UUID userId,
                                  @Param("interestTag") String interestTag);
}