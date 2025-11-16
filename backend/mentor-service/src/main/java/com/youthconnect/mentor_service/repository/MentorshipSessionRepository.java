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
import java.util.UUID;

/**
 * ============================================================================
 * MENTORSHIP SESSION REPOSITORY
 * ============================================================================
 * @author Douglas Kings Kato
 * @since 2025-11-07
 * ============================================================================
 */
@Repository
public interface MentorshipSessionRepository extends JpaRepository<MentorshipSession, UUID> {

    // Paginated queries
    Page<MentorshipSession> findByMentorIdOrMenteeId(
            UUID mentorId,
            UUID menteeId,
            Pageable pageable
    );

    Page<MentorshipSession> findByMentorId(UUID mentorId, Pageable pageable);

    Page<MentorshipSession> findByMenteeId(UUID menteeId, Pageable pageable);

    // Non-paginated (for internal use only)
    List<MentorshipSession> findByMentorIdOrMenteeId(UUID mentorId, UUID menteeId);

    List<MentorshipSession> findByMentorId(UUID mentorId);

    List<MentorshipSession> findByMenteeId(UUID menteeId);

    List<MentorshipSession> findByStatus(MentorshipSession.SessionStatus status);

    List<MentorshipSession> findByMentorIdAndStatus(
            UUID mentorId,
            MentorshipSession.SessionStatus status
    );

    List<MentorshipSession> findByMenteeIdAndStatus(
            UUID menteeId,
            MentorshipSession.SessionStatus status
    );

    List<MentorshipSession> findByStatusAndSessionDatetimeBetween(
            MentorshipSession.SessionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("SELECT ms FROM MentorshipSession ms WHERE ms.mentorId = :mentorId " +
            "AND ms.sessionDatetime >= :fromDate " +
            "AND ms.status IN ('SCHEDULED', 'IN_PROGRESS') " +
            "ORDER BY ms.sessionDatetime ASC")
    Page<MentorshipSession> findUpcomingSessionsByMentor(
            @Param("mentorId") UUID mentorId,
            @Param("fromDate") LocalDateTime fromDate,
            Pageable pageable
    );

    @Query("SELECT ms FROM MentorshipSession ms WHERE ms.menteeId = :menteeId " +
            "AND ms.sessionDatetime >= :fromDate " +
            "AND ms.status IN ('SCHEDULED', 'IN_PROGRESS') " +
            "ORDER BY ms.sessionDatetime ASC")
    Page<MentorshipSession> findUpcomingSessionsByMentee(
            @Param("menteeId") UUID menteeId,
            @Param("fromDate") LocalDateTime fromDate,
            Pageable pageable
    );

    @Query("SELECT ms FROM MentorshipSession ms WHERE ms.mentorId = :mentorId " +
            "AND ms.status IN ('SCHEDULED', 'IN_PROGRESS') " +
            "AND ms.sessionDatetime < :endTime " +
            "AND (ms.sessionDatetime + (ms.durationMinutes * INTERVAL '1 minute')) > :startTime")
    List<MentorshipSession> findConflictingSessions(
            @Param("mentorId") UUID mentorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT COUNT(ms) FROM MentorshipSession ms WHERE ms.mentorId = :mentorId " +
            "AND ms.sessionDatetime BETWEEN :fromDate AND :toDate " +
            "AND ms.status = :status")
    long countSessionsInDateRange(
            @Param("mentorId") UUID mentorId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") MentorshipSession.SessionStatus status
    );

    long countByMentorId(UUID mentorId);

    long countByMentorIdAndStatus(
            UUID mentorId,
            MentorshipSession.SessionStatus status
    );

    long countByMenteeIdAndStatus(
            UUID menteeId,
            MentorshipSession.SessionStatus status
    );

    Optional<MentorshipSession> findBySessionIdAndMentorId(
            UUID sessionId,
            UUID mentorId
    );

    Optional<MentorshipSession> findBySessionIdAndMenteeId(
            UUID sessionId,
            UUID menteeId
    );

    @Query("SELECT ms FROM MentorshipSession ms WHERE ms.status = :status " +
            "AND ms.sessionDatetime BETWEEN :fromTime AND :toTime " +
            "ORDER BY ms.sessionDatetime ASC")
    List<MentorshipSession> findSessionsNeedingReminders(
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("status") MentorshipSession.SessionStatus status
    );

    @Query("SELECT ms FROM MentorshipSession ms WHERE ms.status = :status " +
            "AND ms.sessionDatetime < :currentTime")
    List<MentorshipSession> findPastSessionsWithStatus(
            @Param("currentTime") LocalDateTime currentTime,
            @Param("status") MentorshipSession.SessionStatus status
    );

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.revieweeId = :mentorId " +
            "AND r.reviewType = 'MENTOR_SESSION' AND r.isApproved = true")
    Double getAverageRatingForMentor(@Param("mentorId") UUID mentorId);

    boolean existsByMentorIdAndMenteeIdAndStatus(
            UUID mentorId,
            UUID menteeId,
            MentorshipSession.SessionStatus status
    );
}