package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * UserActivityLogRepository - User Activity Tracking Data Layer
 *
 * Central repository for tracking all user interactions across the Youth Connect platform.
 * This data powers the AI recommendation engine, analytics dashboards, and personalization features.
 *
 * Tracked Activities:
 * - VIEW_OPPORTUNITY: User viewed opportunity details
 * - APPLY_OPPORTUNITY: User submitted application
 * - LISTEN_AUDIO: User played learning module audio (multilingual content)
 * - COMPLETE_MODULE: User finished learning content
 * - BOOK_MENTOR: User scheduled mentorship session
 * - SEARCH_CONTENT: User performed search query
 * - UPDATE_PROFILE: User modified their profile
 * - LOGIN: User authenticated successfully
 * - VIEW_MENTOR: User viewed mentor profile
 * - CONTACT_MENTOR: User initiated mentor contact
 *
 * Data Usage:
 * - AI recommendations (collaborative filtering)
 * - User engagement analytics
 * - Feature usage tracking
 * - Security audit trail
 * - Personalized content delivery
 *
 * Database Table: user_activity_logs
 * Retention: 90 days (configurable)
 * Partitioning: By created_at for performance
 * Indexes: user_id, target_type+target_id, activity_type, created_at
 *
 * Performance Notes:
 * - All queries are optimized with proper indexes
 * - Pagination recommended for large result sets
 * - Native queries used for complex analytics
 * - Session tracking enables detailed user journey analysis
 *
 * @author Douglas Kings Kato
 * @version 1.1.0 - FIXED: findInteractedTargets query syntax
 */
@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    // ========================================================================
    // BASIC ACTIVITY RETRIEVAL
    // ========================================================================

    /**
     * Find all activities for a specific user with pagination
     * Returns most recent activities first
     *
     * Use Case: User activity timeline, audit trail, "Your Activity" page
     * Performance: Uses index on user_id and created_at
     *
     * Example:
     * <pre>
     * Pageable pageable = PageRequest.of(0, 20);
     * Page<UserActivityLog> activities = repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
     * </pre>
     *
     * @param userId User's unique identifier (UUID)
     * @param pageable Pagination parameters (page number, size, sort)
     * @return Page of activity logs ordered by timestamp descending
     */
    Page<UserActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find activities by type for a specific user
     * Useful for analyzing specific user behaviors and patterns
     *
     * Use Case:
     * - Find all opportunities a user viewed
     * - Track learning module completion rate
     * - Analyze mentor interaction patterns
     *
     * Performance: Composite index on (user_id, activity_type, created_at)
     *
     * Example:
     * <pre>
     * Page<UserActivityLog> opportunities = repository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(
     *     userId, "VIEW_OPPORTUNITY", pageable
     * );
     * </pre>
     *
     * @param userId User's unique identifier
     * @param activityType Activity type constant (e.g., "VIEW_OPPORTUNITY", "LOGIN")
     * @param pageable Pagination parameters
     * @return Page of matching activity logs ordered by timestamp descending
     */
    Page<UserActivityLog> findByUserIdAndActivityTypeOrderByCreatedAtDesc(
            UUID userId, String activityType, Pageable pageable);

    /**
     * Find recent activities within a time window
     * Used for real-time analytics, user session tracking, and active user detection
     *
     * Use Case:
     * - Display recent activity in user dashboard
     * - Track real-time engagement metrics
     * - Identify active vs inactive users
     *
     * Performance: Index on (user_id, created_at)
     *
     * Example:
     * <pre>
     * LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
     * List<UserActivityLog> recentActivities = repository.findRecentActivities(userId, last24Hours);
     * </pre>
     *
     * @param userId User's unique identifier
     * @param since Start datetime for filtering (e.g., 24 hours ago)
     * @return List of recent activities ordered by timestamp descending
     */
    @Query("SELECT ual FROM UserActivityLog ual WHERE ual.userId = :userId " +
            "AND ual.createdAt >= :since ORDER BY ual.createdAt DESC")
    List<UserActivityLog> findRecentActivities(@Param("userId") UUID userId,
                                               @Param("since") LocalDateTime since);

    // ========================================================================
    // TARGET-SPECIFIC QUERIES (WHAT USER INTERACTED WITH)
    // ========================================================================

    /**
     * Find all activities for a specific target entity
     * Shows who interacted with a particular resource (opportunity, module, mentor)
     *
     * Use Case:
     * - Track opportunity popularity and engagement
     * - Identify most viewed learning modules
     * - Analyze mentor profile views
     *
     * Performance: Index on (target_type, target_id)
     *
     * Example:
     * <pre>
     * // Find all users who viewed opportunity #123
     * List<UserActivityLog> activities = repository.findByTarget("opportunity", 123L);
     * </pre>
     *
     * @param targetType Type of target entity ("opportunity", "module", "mentor", "post")
     * @param targetId ID of the target entity
     * @return List of all interactions with this target, ordered by timestamp
     */
    @Query("SELECT ual FROM UserActivityLog ual " +
            "WHERE ual.targetType = :targetType AND ual.targetId = :targetId " +
            "ORDER BY ual.createdAt DESC")
    List<UserActivityLog> findByTarget(@Param("targetType") String targetType,
                                       @Param("targetId") Long targetId);

    /**
     * Count unique users who interacted with a target
     * Used for engagement metrics and popularity indicators
     *
     * Use Case:
     * - Show "150 users viewed this opportunity"
     * - Calculate content popularity score
     * - Identify trending content
     *
     * Performance: Optimized with DISTINCT COUNT
     *
     * Example:
     * <pre>
     * long viewCount = repository.countUniqueUsersByTarget("opportunity", 123L);
     * // Display: "Viewed by 150 users"
     * </pre>
     *
     * @param targetType Type of target entity
     * @param targetId ID of the target entity
     * @return Number of unique users who interacted with this target
     */
    @Query("SELECT COUNT(DISTINCT ual.userId) FROM UserActivityLog ual " +
            "WHERE ual.targetType = :targetType AND ual.targetId = :targetId")
    long countUniqueUsersByTarget(@Param("targetType") String targetType,
                                  @Param("targetId") Long targetId);

    /**
     * Check if user has previously interacted with a specific target
     * Quick lookup for "Have you seen this before?" checks
     *
     * Use Case:
     * - Mark opportunities as "Previously Viewed"
     * - Skip duplicate recommendations
     * - Track user interaction history
     *
     * Performance: Very fast - uses EXISTS query with composite index
     *
     * Example:
     * <pre>
     * boolean hasViewed = repository.existsByUserIdAndActivityTypeAndTargetTypeAndTargetId(
     *     userId, "VIEW_OPPORTUNITY", "opportunity", 123L
     * );
     * if (hasViewed) {
     *     // Show "You've seen this before" badge
     * }
     * </pre>
     *
     * @param userId User's unique identifier
     * @param activityType Activity type (e.g., "VIEW_OPPORTUNITY")
     * @param targetType Target entity type (e.g., "opportunity")
     * @param targetId Target entity ID
     * @return true if interaction exists, false otherwise
     */
    boolean existsByUserIdAndActivityTypeAndTargetTypeAndTargetId(
            UUID userId, String activityType, String targetType, Long targetId);

    // ========================================================================
    // AI RECOMMENDATION QUERIES
    // ========================================================================

    /**
     * Find users with similar activity patterns (Collaborative Filtering)
     * Core query for the AI recommendation engine
     *
     * Algorithm:
     * 1. Find all targets the given user interacted with
     * 2. Find other users who also interacted with those targets
     * 3. Rank by number of common interactions
     * 4. Return top N most similar users
     *
     * Use Case:
     * - "Users like you also viewed..."
     * - Collaborative filtering recommendations
     * - User similarity scoring for personalization
     *
     * Performance: Optimized self-join with GROUP BY aggregation
     *
     * Example:
     * <pre>
     * // Find 50 users with similar opportunity viewing patterns
     * List<Object[]> similarUsers = repository.findSimilarUsers(
     *     userId, "VIEW_OPPORTUNITY", 50
     * );
     * for (Object[] row : similarUsers) {
     *     UUID similarUserId = (UUID) row[0];
     *     Long commonInteractions = (Long) row[1];
     *     // Use similarUserId to find what they viewed that current user hasn't
     * }
     * </pre>
     *
     * @param userId User's unique identifier
     * @param activityType Activity type to compare (e.g., "VIEW_OPPORTUNITY")
     * @param limit Maximum number of similar users to return (recommended: 50-100)
     * @return List of [userId, commonInteractionCount] ordered by similarity descending
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
     * Find trending targets based on recent popularity
     * Used for "Trending Now" sections and default recommendations
     *
     * Algorithm:
     * 1. Count interactions for each target within time window
     * 2. Sort by interaction count descending
     * 3. Return top N most popular targets
     *
     * Use Case:
     * - "Trending Opportunities"
     * - "Most Popular Modules"
     * - "Hot Topics"
     * - Default recommendations for new users
     *
     * Performance: Aggregation query with time-based filtering
     *
     * Example:
     * <pre>
     * // Find top 10 trending opportunities from last 7 days
     * LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
     * List<Object[]> trending = repository.findTrendingTargets("opportunity", lastWeek, 10);
     * for (Object[] row : trending) {
     *     Long targetId = (Long) row[0];
     *     Long interactionCount = (Long) row[1];
     *     // Load opportunity details and display with "🔥 Trending" badge
     * }
     * </pre>
     *
     * @param targetType Type of target ("opportunity", "module", "mentor")
     * @param since Start date for trending calculation (e.g., 7 days ago)
     * @param limit Maximum results to return (recommended: 10-20)
     * @return List of [targetId, interactionCount] ordered by popularity descending
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
     * FIXED: Find targets that a user HAS interacted with
     * Used to filter out already-seen content from recommendations
     *
     * Original Issue: Query syntax error - "Could not interpret path expression 't'"
     * Fix: Changed to proper JPQL syntax with correct alias usage
     *
     * Algorithm:
     * 1. Look for user's activity logs matching target type
     * 2. Filter to only targets in the provided list
     * 3. Return list of target IDs user has already seen
     * 4. Caller then subtracts these from all available targets
     *
     * Use Case:
     * - Generate "New for You" recommendations
     * - Filter out already-viewed opportunities
     * - Ensure content freshness in recommendations
     *
     * Performance: IN clause with index on (user_id, target_type, target_id)
     *
     * Example:
     * <pre>
     * // Get all available opportunity IDs
     * List<Long> allOpportunityIds = opportunityService.getAllIds();
     *
     * // Find which ones user has already viewed
     * List<Long> viewedIds = repository.findInteractedTargets(
     *     userId, "opportunity", allOpportunityIds
     * );
     *
     * // Calculate unseen opportunities
     * allOpportunityIds.removeAll(viewedIds);
     * // Now allOpportunityIds contains only NEW opportunities for this user
     * </pre>
     *
     * @param userId User's unique identifier
     * @param targetType Type of targets to check (e.g., "opportunity", "module")
     * @param allTargetIds List of all available target IDs to check against
     * @return List of target IDs that user HAS interacted with
     */
    @Query("SELECT ual.targetId FROM UserActivityLog ual " +
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
     * Count activities by type for a user within a time period
     * Used for user engagement scoring and behavior analysis
     *
     * Use Case:
     * - Generate user activity reports
     * - Calculate engagement scores
     * - Identify most common user actions
     * - A/B testing analysis
     *
     * Example:
     * <pre>
     * LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
     * List<Object[]> activityBreakdown = repository.countActivitiesByType(userId, last30Days);
     *
     * for (Object[] row : activityBreakdown) {
     *     String activityType = (String) row[0];
     *     Long count = (Long) row[1];
     *     System.out.println(activityType + ": " + count + " times");
     * }
     * // Output:
     * // VIEW_OPPORTUNITY: 45 times
     * // APPLY_OPPORTUNITY: 12 times
     * // LISTEN_AUDIO: 23 times
     * </pre>
     *
     * @param userId User's unique identifier
     * @param since Start date for counting
     * @return List of [activityType, count] ordered by count descending
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
     * Calculate user engagement score based on activity frequency
     * Higher score indicates more active/engaged user
     *
     * Scoring Logic:
     * - Simple count of all activities in time period
     * - Can be weighted by activity importance in calling code
     * - Useful for user segmentation (high/medium/low engagement)
     *
     * Use Case:
     * - Identify power users for beta testing
     * - Trigger re-engagement campaigns for inactive users
     * - Gamification leaderboards
     * - User cohort analysis
     *
     * Example:
     * <pre>
     * LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
     * long score = repository.calculateEngagementScore(userId, last30Days);
     *
     * String userSegment;
     * if (score > 100) {
     *     userSegment = "Power User 🔥";
     * } else if (score > 20) {
     *     userSegment = "Active User ✓";
     * } else {
     *     userSegment = "Casual User";
     * }
     * </pre>
     *
     * @param userId User's unique identifier
     * @param since Start date for calculation (e.g., 30 days ago)
     * @return Activity count (raw engagement score)
     */
    @Query("SELECT COUNT(ual) FROM UserActivityLog ual " +
            "WHERE ual.userId = :userId AND ual.createdAt >= :since")
    long calculateEngagementScore(@Param("userId") UUID userId,
                                  @Param("since") LocalDateTime since);

    /**
     * Find most active users in a time period
     * Used for gamification, leaderboards, and community recognition
     *
     * Use Case:
     * - "Top Contributors This Month" leaderboard
     * - Identify engaged users for ambassador programs
     * - Community engagement metrics
     * - Platform health monitoring
     *
     * Example:
     * <pre>
     * LocalDateTime thisMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0);
     * List<Object[]> topUsers = repository.findMostActiveUsers(thisMonth, 10);
     *
     * System.out.println("🏆 Top 10 Most Active Users This Month:");
     * int rank = 1;
     * for (Object[] row : topUsers) {
     *     UUID userId = (UUID) row[0];
     *     Long activityCount = (Long) row[1];
     *     User user = userService.findById(userId);
     *     System.out.println(rank++ + ". " + user.getName() + " - " + activityCount + " activities");
     * }
     * </pre>
     *
     * @param since Start date for period
     * @param limit Maximum number of users to return
     * @return List of [userId, activityCount] ordered by activity count descending
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
     * Calculate conversion rate (views → applications)
     * Percentage of opportunities that were applied to after viewing
     *
     * Conversion Formula:
     * (Unique opportunities applied to) / (Unique opportunities viewed)
     *
     * Use Case:
     * - Measure user intent and quality of opportunities
     * - A/B test opportunity presentation formats
     * - User behavior analytics
     * - Platform effectiveness metrics
     *
     * Example:
     * <pre>
     * LocalDateTime last90Days = LocalDateTime.now().minusDays(90);
     * Double conversionRate = repository.calculateConversionRate(userId, last90Days);
     *
     * if (conversionRate != null) {
     *     double percentage = conversionRate * 100;
     *     System.out.printf("Conversion Rate: %.1f%%\n", percentage);
     *
     *     if (percentage < 5) {
     *         // Low conversion - improve opportunity matching
     *         recommendationService.recalibrateForUser(userId);
     *     }
     * }
     * </pre>
     *
     * @param userId User's unique identifier
     * @param since Start date for calculation
     * @return Conversion rate as decimal (0.0 to 1.0), or null if no views
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
     * Use Case:
     * - Analyze user journey within a session
     * - Debug user experience issues
     * - Session timeout detection
     * - Cross-device session tracking
     *
     * Session ID Sources:
     * - USSD: Generated per USSD session
     * - Web: JWT token ID or session cookie
     * - Mobile: Device-specific session ID
     *
     * Example:
     * <pre>
     * List<UserActivityLog> sessionActivities = repository.findBySessionId(sessionId);
     *
     * System.out.println("User Session Journey:");
     * for (UserActivityLog activity : sessionActivities) {
     *     System.out.println(activity.getCreatedAt() + " - " + activity.getActivityType());
     * }
     * // Output:
     * // 10:30:15 - LOGIN
     * // 10:30:45 - VIEW_OPPORTUNITY
     * // 10:31:20 - APPLY_OPPORTUNITY
     * </pre>
     *
     * @param sessionId Session identifier (UUID string)
     * @return List of activities in chronological order
     */
    @Query("SELECT ual FROM UserActivityLog ual WHERE ual.sessionId = :sessionId " +
            "ORDER BY ual.createdAt ASC")
    List<UserActivityLog> findBySessionId(@Param("sessionId") String sessionId);

    /**
     * Find user's most recent login activity
     * Used for "Last seen" features and session management
     *
     * Use Case:
     * - Display "Last active: 2 hours ago"
     * - Detect stale sessions
     * - Security audit - track login patterns
     * - Re-engagement triggers
     *
     * Example:
     * <pre>
     * UserActivityLog lastLogin = repository.findLastLogin(userId);
     * if (lastLogin != null) {
     *     Duration timeSinceLogin = Duration.between(
     *         lastLogin.getCreatedAt(),
     *         LocalDateTime.now()
     *     );
     *
     *     if (timeSinceLogin.toDays() > 7) {
     *         // User hasn't logged in for a week - send re-engagement email
     *         notificationService.sendReEngagementEmail(userId);
     *     }
     * }
     * </pre>
     *
     * @param userId User's unique identifier
     * @return Most recent login activity, or null if user never logged in
     */
    @Query("SELECT ual FROM UserActivityLog ual WHERE ual.userId = :userId " +
            "AND ual.activityType = 'LOGIN' " +
            "ORDER BY ual.createdAt DESC LIMIT 1")
    UserActivityLog findLastLogin(@Param("userId") UUID userId);

    // ========================================================================
    // MAINTENANCE & ADMIN QUERIES
    // ========================================================================

    /**
     * Delete old activity logs for data retention compliance
     * Should be run periodically via scheduled job (e.g., weekly)
     *
     * Data Retention Policy:
     * - Keep logs for 90 days by default
     * - Configurable per deployment
     * - GDPR/data privacy compliance
     *
     * WARNING: This is a DESTRUCTIVE operation
     * - Ensure backups exist before running
     * - Run during low-traffic periods
     * - Monitor execution time for large deletes
     *
     * Use Case:
     * - Scheduled cleanup job
     * - Database size management
     * - Compliance with data retention policies
     *
     * Example:
     * <pre>
     * // Delete logs older than 90 days
     * LocalDateTime retentionCutoff = LocalDateTime.now().minusDays(90);
     * long deletedCount = repository.deleteByCreatedAtBefore(retentionCutoff);
     *
     * log.info("Deleted {} old activity logs (older than {})",
     *          deletedCount, retentionCutoff);
     * </pre>
     *
     * @param before Delete activities older than this date
     * @return Number of records deleted
     */
    @Transactional
    @Modifying
    long deleteByCreatedAtBefore(LocalDateTime before);

    /**
     * Count total activities for monitoring and admin dashboards
     * Used to track platform usage growth over time
     *
     * Use Case:
     * - Admin dashboard metrics
     * - Platform health monitoring
     * - Growth tracking (daily/weekly/monthly active users)
     * - Capacity planning
     *
     * Example:
     * <pre>
     * LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0);
     * long activitiesToday = repository.countByCreatedAtAfter(today);
     *
     * LocalDateTime last7Days = LocalDateTime.now().minusDays(7);
     * long activitiesLast7Days = repository.countByCreatedAtAfter(last7Days);
     *
     * System.out.println("Entrepreneurship Booster Platform Activity Metrics:");
     * System.out.println("Today: " + activitiesToday + " activities");
     * System.out.println("Last 7 days: " + activitiesLast7Days + " activities");
     * </pre>
     *
     * @param since Start date for counting
     * @return Total activity count since date
     */
    long countByCreatedAtAfter(LocalDateTime since);
}