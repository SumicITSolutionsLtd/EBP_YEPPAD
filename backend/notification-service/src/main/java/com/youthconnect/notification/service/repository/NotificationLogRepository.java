package com.youthconnect.notification.service.repository;

import com.youthconnect.notification.service.entity.NotificationLog;
import com.youthconnect.notification.service.entity.NotificationLog.NotificationStatus;
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
 * ═══════════════════════════════════════════════════════════════════════════
 * NOTIFICATION LOG REPOSITORY - Data Access Layer (With Pagination)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Provides query methods for fetching, tracking, and monitoring notifications.
 *
 * Changes from Original:
 * - Added Page<> return types for pagination (as per guidelines)
 * - Changed ID type from Long → UUID
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Microservices guidelines compliant)
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    /**
     * Retrieves paginated notifications for a specific user
     * Ordered by creation date (latest first)
     *
     * @param userId   User UUID
     * @param pageable Pagination information
     * @return Paginated list of notifications
     */
    Page<NotificationLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Finds failed notifications eligible for retry
     * Checks both retry count and scheduled retry time
     *
     * @param now      Current timestamp
     * @param pageable Pagination information
     * @return Paginated list of failed notifications pending retry
     */
    @Query("SELECT n FROM NotificationLog n " +
            "WHERE n.status = 'FAILED' " +
            "AND n.retryCount < n.maxRetries " +
            "AND n.nextRetryAt <= :now")
    Page<NotificationLog> findPendingRetries(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Retrieves paginated notifications by status within a date range
     *
     * @param status    Notification status
     * @param startDate Start of date range
     * @param endDate   End of date range
     * @param pageable  Pagination information
     * @return Paginated notifications matching criteria
     */
    @Query("SELECT n FROM NotificationLog n " +
            "WHERE n.status = :status " +
            "AND n.createdAt BETWEEN :startDate AND :endDate")
    Page<NotificationLog> findByStatusAndDateRange(
            @Param("status") NotificationStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Counts notifications with given status created after specific time
     * Used for monitoring and analytics
     *
     * @param status Notification status
     * @param after  Cutoff timestamp
     * @return Count of matching notifications
     */
    long countByStatusAndCreatedAtAfter(NotificationStatus status, LocalDateTime after);

    /**
     * Counts all notifications by status (no time filtering)
     *
     * @param status Notification status
     * @return Total count of notifications with given status
     */
    @Query("SELECT COUNT(n) FROM NotificationLog n WHERE n.status = :status")
    Long countByStatus(@Param("status") NotificationStatus status);

    /**
     * Retrieves recent notifications for a user created after specific timestamp
     * Results are paginated and ordered by creation date (latest first)
     *
     * @param userId   User UUID
     * @param since    Timestamp to filter notifications created after
     * @param pageable Pagination information
     * @return Paginated recent notifications for the user
     */
    @Query("SELECT n FROM NotificationLog n " +
            "WHERE n.userId = :userId " +
            "AND n.createdAt > :since " +
            "ORDER BY n.createdAt DESC")
    Page<NotificationLog> findRecentNotificationsByUser(
            @Param("userId") UUID userId,
            @Param("since") LocalDateTime since,
            Pageable pageable
    );
}