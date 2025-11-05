package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * UserActivityLogRepository - User Activity Tracking Data Layer
 *
 * Central repository for tracking all user interactions across the platform.
 * This data powers the AI recommendation engine and analytics dashboards.
 *
 * Tracked Activities:
 * - VIEW_OPPORTUNITY: User viewed opportunity details
 * - APPLY_OPPORTUNITY: User submitted application
 * - LISTEN_AUDIO: User played learning module audio
 * - COMPLETE_MODULE: User finished learning content
 * - BOOK_MENTOR: User scheduled mentorship session
 * - SEARCH_CONTENT: User performed search query
 * - UPDATE_PROFILE: User modified their profile
 * - LOGIN: User authenticated successfully
 *
 * Data Usage:
 * - AI recommendations (collaborative filtering)
 * - User engagement analytics
 * - Feature usage tracking
 * - Security audit trail
 *
 * Database Table: user_activity_logs
 * Retention: 90 days (configurable)
 * Partitioning: By created_at for performance
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    // ========================================================================
    // BASIC ACTIVITY RETRIEVAL
    // ========================================================================

    /**
     * Find all activities for a specific user
     * Returns most recent activities first
     *
     * Use Case: User activity timeline, audit trail
     *
     * @param userId User's unique identifier
     * @param pageable Pagination parameters
     * @return Page of activity logs
     */
    Page<UserActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find activities by type for a user
     * Useful for analyzing specific user behaviors
     *
     * Example: Find all opportunities a user viewed
     *
     * @param userId User's unique identifier
     * @param activityType Activity type constant (e.g., "VIEW_OPPORTUNITY")
     * @param pageable Pagination parameters
     * @return Page of matching activity logs
     */
    Page<UserActivityLog> findByUserIdAndActivityTypeOrderByCreatedAtDesc(
            UUID userId, String activityType, Pageable pageable);

    /**
     * Find recent activities within time window
     * Used for real-time analytics and user session tracking
     *
     * @param userId User's unique identifier
     * @param since Start datetime for filtering
     * @return List of recent activities
     */
    @Query("SELECT ual FROM UserActivityLog ual WHERE ual.userId = :userId " +
            "AND ual.createdAt >= :since ORDER BY ual.createdAt DESC")
    List<UserActivityLog> findRecentActivities(@Param("userId") UUID userId,
                                               @Param("since") LocalDateTime since);

    // ========================================================================
    // TARGET-SPECIFIC QUERIES (WHAT USER INTERACTED WITH)
    // ========================================================================

    /**
     * Find all activities for a specific target
     * Example: All users who viewed opportunity #123
     *
     * Use Case: Opportunity popularity tracking, engagement metrics
     *
     * @param targetType Type of target entity ("opportunity", "module", "mentor")
     * @param targetId ID of the target entity
     * @return List of all interactions with this target
     */
    @Query("SELECT ual FROM UserActivityLog ual " +
            "WHERE ual.targetType = :targetType AND ual.targetId = :targetId " +
            "ORDER BY ual.createdAt DESC")
    List<UserActivityLog> findByTarget(@Param("targetType") String targetType,
                                       @Param("targetId") Long targetId);

    /**
     * Count unique users who interacted with a target
     * Used for engagement metrics (e.g., "150 users viewed this opportunity")
     *
     * @param targetType Type of target entity
     * @param targetId ID of the target entity
     * @return Number of unique users
     */
    @Query("SELECT COUNT(DISTINCT ual.userId) FROM UserActivityLog ual " +
            "WHERE ual.targetType = :targetType AND ual.targetId = :targetId")
    long countUniqueUsersByTarget(@Param("targetType") String targetType,
                                  @Param("targetId") Long targetId);

    /**
     * Check if user has interacted with specific target
     * Quick lookup for "Have you viewed this before?" checks
     *
     * @param userId User's unique identifier
     * @param activityType Activity type
     * @param targetType Target entity type
     * @param targetId Target entity ID
     * @return true if interaction exists
     */
    boolean existsByUserIdAndActivityTypeAndTargetTypeAndTargetId(
            UUID userId, String activityType, String targetType, Long targetId);

    // ========================================================================
    // AI RECOMMENDATION QUERIES
    // ========================================================================

    /**
     * Find users with similar activity patterns
     * Core query for collaborative filtering recommendations
     *
     * Returns users who interacted with the same targets as the given user
     *
     * @param userId User's unique identifier
     * @param activityType Activity type to compare
     * @param limit Maximum similar users to return
     * @return List of similar user IDs ordered by similarity
     */
    @Query(value = "SELECT ual2.user_id, COUNT(*) as common_interactions " +
            "FROM user_activity_logs ual1 " +
            "JOIN user_activity_logs ual2 ON ual1.target_id = ual2.target_id " +
            "                             AND ual1.target_type = ual2.target_type " +
            "WHERE ual1.user_id = :userId " +
            "AND ual2.user_id != :userId " +
            "AND ual1.activity_type = :activityType " +
            "AND ual2.activity_type = :activityType " +
            "GROUP BY ual2.user_id " +
            "ORDER BY common_interactions DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findSimilarUsers(@Param("userId") UUID userId,
                                    @Param("activityType") String activityType,
                                    @Param("limit") int limit);

    /**
     * Find popular targets of a specific type
     * Used for "Trending" sections and default recommendations
     *
     * @param targetType Type of target ("opportunity", "module", etc.)
     * @param since Start date for trending calculation
     * @param limit Maximum results
     * @return List of [targetId, interactionCount] ordered by popularity
     */
    @Query(value = "SELECT target_id, COUNT(*) as interaction_count " +
            "FROM user_activity_logs " +
            "WHERE target_type = :targetType " +
            "AND created_at >= :since " +
            "GROUP BY target_id " +
            "ORDER BY interaction_count DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTrendingTargets(@Param("targetType") String targetType,
                                       @Param("since") LocalDateTime since,
                                       @Param("limit") int limit);

    /**
     * Find targets user has NOT interacted with yet
     * Used for discovering new content for users
     *
     * @param userId User's unique identifier
     * @param targetType Type of targets to find
     * @param allTargetIds List of all available target IDs
     * @return List of target IDs user hasn't seen
     */
    @Query("SELECT t FROM UserActivityLog ual " +
            "WHERE ual.userId = :userId " +
            "AND ual.targetType = :targetType " +
            "AND ual.targetId IN :allTargetIds")
    List<Long> findInteractedTargets(@Param("userId") UUID userId,
                                     @Param("targetType") String targetType,
                                     @Param("allTargetIds") List<Long> allTargetIds);

    // ========================================================================
    // ANALYTICS & METRICS QUERIES
    // ========================================================================

    /**
     * Count activities by type for a user
     * Used for user engagement scoring
     *
     * @param userId User's unique identifier
     * @param since Start date for counting
     * @return List of [activityType, count]
     */
    @Query(value = "SELECT activity_type, COUNT(*) as count " +
            "FROM user_activity_logs " +
            "WHERE user_id = :userId " +
            "AND created_at >= :since " +
            "GROUP BY activity_type " +
            "ORDER BY count DESC",
            nativeQuery = true)
    List<Object[]> countActivitiesByType(@Param("userId") UUID userId,
                                         @Param("since") LocalDateTime since);

    /**
     * Get user engagement score based on activity frequency
     * Higher score = more active user
     *
     * @param userId User's unique identifier
     * @param since Start date for calculation
     * @return Activity count (engagement score)
     */
    @Query("SELECT COUNT(ual) FROM UserActivityLog ual " +
            "WHERE ual.userId = :userId AND ual.createdAt >= :since")
    long calculateEngagementScore(@Param("userId") UUID userId,
                                  @Param("since") LocalDateTime since);

    /**
     * Find most active users in a time period
     * Used for gamification and leaderboards
     *
     * @param since Start date
     * @param limit Maximum results
     * @return List of [userId, activityCount]
     */
    @Query(value = "SELECT user_id, COUNT(*) as activity_count " +
            "FROM user_activity_logs " +
            "WHERE created_at >= :since " +
            "GROUP BY user_id " +
            "ORDER BY activity_count DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findMostActiveUsers(@Param("since") LocalDateTime since,
                                       @Param("limit") int limit);

    /**
     * Calculate conversion rate (views → applies)
     * Percentage of opportunities that were applied to after viewing
     *
     * @param userId User's unique identifier
     * @param since Start date for calculation
     * @return Conversion rate as decimal (0.0 to 1.0)
     */
    @Query(value = "SELECT " +
            "CAST(COUNT(DISTINCT CASE WHEN activity_type = 'APPLY_OPPORTUNITY' " +
            "THEN target_id END) AS FLOAT) / " +
            "NULLIF(COUNT(DISTINCT CASE WHEN activity_type = 'VIEW_OPPORTUNITY' " +
            "THEN target_id END), 0) " +
            "FROM user_activity_logs " +
            "WHERE user_id = :userId " +
            "AND created_at >= :since",
            nativeQuery = true)
    Double calculateConversionRate(@Param("userId") UUID userId,
                                   @Param("since") LocalDateTime since);

    // ========================================================================
    // SESSION TRACKING
    // ========================================================================

    /**
     * Find all activities in a specific session
     * Sessions are tracked by session_id from USSD or web app
     *
     * @param sessionId Session identifier
     * @return List of activities in this session
     */
    @Query("SELECT ual FROM UserActivityLog ual WHERE ual.sessionId = :sessionId " +
            "ORDER BY ual.createdAt ASC")
    List<UserActivityLog> findBySessionId(@Param("sessionId") String sessionId);

    /**
     * Find user's last login activity
     * Used for "Last seen" features
     *
     * @param userId User's unique identifier
     * @return Most recent login activity
     */
    @Query("SELECT ual FROM UserActivityLog ual WHERE ual.userId = :userId " +
            "AND ual.activityType = 'LOGIN' " +
            "ORDER BY ual.createdAt DESC LIMIT 1")
    UserActivityLog findLastLogin(@Param("userId") UUID userId);

    // ========================================================================
    // MAINTENANCE QUERIES
    // ========================================================================

    /**
     * Delete old activity logs for data retention compliance
     * Should be run periodically via scheduled job
     *
     * WARNING: Destructive operation
     *
     * @param before Delete activities older than this date
     * @return Number of records deleted
     */
    long deleteByCreatedAtBefore(LocalDateTime before);

    /**
     * Count total activities for monitoring
     * Used in admin dashboards
     *
     * @param since Start date
     * @return Total activity count
     */
    long countByCreatedAtAfter(LocalDateTime since);
}