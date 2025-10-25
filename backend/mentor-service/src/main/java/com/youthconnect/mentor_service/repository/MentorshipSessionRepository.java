package com.youthconnect.mentor_service.repository;

import com.youthconnect.mentor_service.entity.MentorshipSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * MENTORSHIP SESSION REPOSITORY
 * ============================================================================
 *
 * Data access layer for mentorship sessions.
 * Provides query methods for session management and analytics.
 *
 * KEY OPERATIONS:
 * - Find sessions by mentor or mentee
 * - Query sessions by status and date range
 * - Check for scheduling conflicts
 * - Calculate mentor session counts
 * - Retrieve upcoming sessions for reminders
 *
 * INDEXING STRATEGY:
 * - Composite index on (mentor_id, session_datetime, status)
 * - Composite index on (mentee_id, status, session_datetime)
 * - Index on (session_datetime, status) for reminder queries
 *
 * @author
 * Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@Repository
public interface MentorshipSessionRepository extends JpaRepository<MentorshipSession, Long> {

    /**
     * Find all sessions for a specific user (mentor or mentee)
     * Used for "My Sessions" page
     */
    List<MentorshipSession> findByMentorIdOrMenteeId(Long mentorId, Long menteeId);

    /**
     * Find sessions by mentor or mentee with pagination
     */
    Page<MentorshipSession> findByMentorIdOrMenteeId(
            Long mentorId,
            Long menteeId,
            Pageable pageable
    );

    /**
     * Find sessions by mentor ID
     */
    List<MentorshipSession> findByMentorId(Long mentorId);

    /**
     * Find sessions by mentee ID
     */
    List<MentorshipSession> findByMenteeId(Long menteeId);

    /**
     * Find sessions by status
     */
    List<MentorshipSession> findByStatus(MentorshipSession.SessionStatus status);

    /**
     * Find sessions by mentor and status
     */
    List<MentorshipSession> findByMentorIdAndStatus(
            Long mentorId,
            MentorshipSession.SessionStatus status
    );

    /**
     * Find sessions by mentee and status
     */
    List<MentorshipSession> findByMenteeIdAndStatus(
            Long menteeId,
            MentorshipSession.SessionStatus status
    );

    /**
     * Find sessions within a date range
     */
    List<MentorshipSession> findByStatusAndSessionDatetimeBetween(
            MentorshipSession.SessionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Find upcoming sessions for a mentor
     */
    @Query("SELECT ms FROM MentorshipSession ms WHERE ms.mentorId = :mentorId " +
            "AND ms.sessionDatetime >= :fromDate " +
            "AND ms.status IN ('SCHEDULED', 'IN_PROGRESS') " +
            "ORDER BY ms.sessionDatetime ASC")
    List<MentorshipSession> findUpcomingSessionsByMentor(
            @Param("mentorId") Long mentorId,
            @Param("fromDate") LocalDateTime fromDate
    );

    /**
     * Find upcoming sessions for mentor with pagination
     */
    @Query("SELECT ms FROM MentorshipSession ms WHERE ms.mentorId = :mentorId " +
            "AND ms.sessionDatetime >= :fromDate " +
            "AND ms.status IN ('SCHEDULED', 'IN_PROGRESS') " +
            "ORDER BY ms.sessionDatetime ASC")
    Page<MentorshipSession> findUpcomingSessionsByMentor(
            @Param("mentorId") Long mentorId,
            @Param("fromDate") LocalDateTime fromDate,
            Pageable pageable
    );

    /**
     * Find upcoming sessions for a mentee
     */
    @Query("SELECT ms FROM MentorshipSession ms WHERE ms.menteeId = :menteeId " +
            "AND ms.sessionDatetime >= :fromDate " +
            "AND ms.status IN ('SCHEDULED', 'IN_PROGRESS') " +
            "ORDER BY ms.sessionDatetime ASC")
    List<MentorshipSession> findUpcomingSessionsByMentee(
            @Param("menteeId") Long menteeId,
            @Param("fromDate") LocalDateTime fromDate
    );

    /**
     * Check for scheduling conflicts
     */
    @Query("SELECT ms FROM MentorshipSession ms WHERE ms.mentorId = :mentorId " +
            "AND ms.status IN ('SCHEDULED', 'IN_PROGRESS') " +
            "AND ((ms.sessionDatetime <= :endTime AND " +
            "DATE_ADD(ms.sessionDatetime, INTERVAL ms.durationMinutes MINUTE) >= :startTime))")
    List<MentorshipSession> findConflictingSessions(
            @Param("mentorId") Long mentorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Count mentor's sessions in a date range
     */
    @Query("SELECT COUNT(ms) FROM MentorshipSession ms WHERE ms.mentorId = :mentorId " +
            "AND ms.sessionDatetime BETWEEN :fromDate AND :toDate " +
            "AND ms.status = :status")
    long countSessionsInDateRange(
            @Param("mentorId") Long mentorId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") MentorshipSession.SessionStatus status
    );

    /**
     * Count total sessions for a mentor
     */
    long countByMentorId(Long mentorId);

    /**
     * Count total completed sessions for a mentor
     */
    long countByMentorIdAndStatus(
            Long mentorId,
            MentorshipSession.SessionStatus status
    );

    /**
     * Count total completed sessions for a mentee
     */
    long countByMenteeIdAndStatus(
            Long menteeId,
            MentorshipSession.SessionStatus status
    );

    /**
     * Find session by ID and mentor ID
     */
    Optional<MentorshipSession> findBySessionIdAndMentorId(
            Long sessionId,
            Long mentorId
    );

    /**
     * Find session by ID and mentee ID
     */
    Optional<MentorshipSession> findBySessionIdAndMenteeId(
            Long sessionId,
            Long menteeId
    );

    /**
     * Find sessions needing reminders
     */
    @Query("SELECT ms FROM MentorshipSession ms WHERE ms.status = :status " +
            "AND ms.sessionDatetime BETWEEN :fromTime AND :toTime " +
            "ORDER BY ms.sessionDatetime ASC")
    List<MentorshipSession> findSessionsNeedingReminders(
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("status") MentorshipSession.SessionStatus status
    );

    /**
     * Find past sessions that haven't been completed
     */
    @Query("SELECT ms FROM MentorshipSession ms WHERE ms.status = :status " +
            "AND ms.sessionDatetime < :currentTime")
    List<MentorshipSession> findPastSessionsWithStatus(
            @Param("currentTime") LocalDateTime currentTime,
            @Param("status") MentorshipSession.SessionStatus status
    );

    /**
     * Get mentor's average session rating
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.revieweeId = :mentorId " +
            "AND r.reviewType = 'MENTOR_SESSION' AND r.isApproved = true")
    Double getAverageRatingForMentor(@Param("mentorId") Long mentorId);

    /**
     * Check if mentor and mentee have had previous sessions
     */
    boolean existsByMentorIdAndMenteeIdAndStatus(
            Long mentorId,
            Long menteeId,
            MentorshipSession.SessionStatus status
    );
}
