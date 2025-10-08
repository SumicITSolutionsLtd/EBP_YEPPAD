package com.youthconnect.notification.service.repository;

import com.youthconnect.notification.service.entity.NotificationLog;
import com.youthconnect.notification.service.entity.NotificationLog.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing {@link NotificationLog} entities.
 * Provides query methods for fetching, tracking, and monitoring notifications.
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /**
     * Retrieves all notifications sent to a specific user, ordered by creation date (latest first).
     *
     * @param userId the ID of the user
     * @return list of notifications for the given user
     */
    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds failed notifications that are eligible for retry,
     * considering both retry count and scheduled retry time.
     *
     * @param now the current timestamp used to check retry eligibility
     * @return list of failed notifications pending retry
     */
    @Query("SELECT n FROM NotificationLog n " +
            "WHERE n.status = 'FAILED' " +
            "AND n.retryCount < n.maxRetries " +
            "AND n.nextRetryAt <= :now")
    List<NotificationLog> findPendingRetries(@Param("now") LocalDateTime now);

    /**
     * Retrieves notifications by status within a given date range.
     *
     * @param status the notification status (e.g., SENT, FAILED, PENDING)
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of notifications matching the given status and date range
     */
    @Query("SELECT n FROM NotificationLog n " +
            "WHERE n.status = :status " +
            "AND n.createdAt BETWEEN :startDate AND :endDate")
    List<NotificationLog> findByStatusAndDateRange(
            @Param("status") NotificationStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Counts notifications with the given status that were created after a specific time.
     * Useful for monitoring recent failures or successes.
     *
     * @param status the notification status
     * @param after the cutoff timestamp
     * @return count of notifications matching criteria
     */
    long countByStatusAndCreatedAtAfter(NotificationStatus status, LocalDateTime after);

    /**
     * Counts all notifications by status (without time filtering).
     *
     * @param status the notification status
     * @return number of notifications matching the given status
     */
    @Query("SELECT COUNT(n) FROM NotificationLog n WHERE n.status = :status")
    Long countByStatus(@Param("status") NotificationStatus status);

    /**
     * Retrieves recent notifications for a user created after a given timestamp.
     * Results are ordered by creation date (latest first).
     *
     * @param userId the ID of the user
     * @param since the timestamp to filter notifications created after
     * @return list of recent notifications for the given user
     */
    @Query("SELECT n FROM NotificationLog n " +
            "WHERE n.userId = :userId " +
            "AND n.createdAt > :since " +
            "ORDER BY n.createdAt DESC")
    List<NotificationLog> findRecentNotificationsByUser(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );
}
