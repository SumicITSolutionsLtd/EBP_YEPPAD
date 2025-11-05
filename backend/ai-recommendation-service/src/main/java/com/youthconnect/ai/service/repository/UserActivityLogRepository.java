package com.youthconnect.ai.service.repository;

import com.youthconnect.ai.service.entity.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for user activity tracking
 * Maps to existing `user_activity_logs` table
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    /**
     * Get user activities by type for behavior analysis
     */
    List<UserActivityLog> findByUserIdAndActivityTypeOrderByCreatedAtDesc(
            Long userId,
            String activityType
    );

    /**
     * Get recent user activities for recommendation algorithms
     */
    @Query("SELECT a FROM UserActivityLog a " +
            "WHERE a.userId = :userId " +
            "AND a.createdAt > :since " +
            "ORDER BY a.createdAt DESC")
    List<UserActivityLog> findRecentActivitiesByUser(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );

    /**
     * Count user sessions for engagement calculation
     */
    @Query("SELECT COUNT(DISTINCT a.sessionId) " +
            "FROM UserActivityLog a " +
            "WHERE a.userId = :userId " +
            "AND a.createdAt > :since")
    Long countDistinctSessionsByUser(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );

    /**
     * Get most popular content types for user
     */
    @Query("SELECT a.targetType, COUNT(a) as count " +
            "FROM UserActivityLog a " +
            "WHERE a.userId = :userId " +
            "GROUP BY a.targetType " +
            "ORDER BY count DESC")
    List<Object[]> findMostPopularContentTypesByUser(@Param("userId") Long userId);

    /**
     * Get user activity pattern by hour of day
     */
    @Query("SELECT FUNCTION('HOUR', a.createdAt) as hour, COUNT(a) as count " +
            "FROM UserActivityLog a " +
            "WHERE a.userId = :userId " +
            "GROUP BY FUNCTION('HOUR', a.createdAt) " +
            "ORDER BY count DESC")
    List<Object[]> findUserActivityPatternByHour(@Param("userId") Long userId);

    /**
     * Find all activities for a specific target
     */
    List<UserActivityLog> findByTargetTypeAndTargetId(String targetType, Long targetId);

    /**
     * Get total activity count for a user in date range
     */
    @Query("SELECT COUNT(a) " +
            "FROM UserActivityLog a " +
            "WHERE a.userId = :userId " +
            "AND a.createdAt BETWEEN :startDate AND :endDate")
    Long countUserActivitiesInRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}