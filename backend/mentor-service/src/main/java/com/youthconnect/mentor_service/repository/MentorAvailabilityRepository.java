package com.youthconnect.mentor_service.repository;

import com.youthconnect.mentor_service.entity.MentorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ============================================================================
 * MENTOR AVAILABILITY REPOSITORY
 * ============================================================================
 * @author Douglas Kings Kato
 * @since 2025-11-07
 * ============================================================================
 */
@Repository
public interface MentorAvailabilityRepository extends JpaRepository<MentorAvailability, UUID> {

    List<MentorAvailability> findByMentorId(UUID mentorId);

    List<MentorAvailability> findByMentorIdAndIsActiveTrue(UUID mentorId);

    List<MentorAvailability> findByMentorIdAndDayOfWeekAndIsActiveTrue(
            UUID mentorId,
            DayOfWeek dayOfWeek
    );

    boolean existsByMentorIdAndDayOfWeekAndIsActiveTrue(
            UUID mentorId,
            DayOfWeek dayOfWeek
    );

    @Query("SELECT ma FROM MentorAvailability ma WHERE ma.mentorId = :mentorId " +
            "AND ma.dayOfWeek = :dayOfWeek " +
            "AND ma.isActive = true " +
            "AND ((ma.startTime < :endTime AND ma.endTime > :startTime))")
    List<MentorAvailability> findOverlappingSlots(
            @Param("mentorId") UUID mentorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    void deleteByMentorId(UUID mentorId);

    long countByMentorIdAndIsActiveTrue(UUID mentorId);

    Optional<MentorAvailability> findByAvailabilityIdAndMentorId(
            UUID availabilityId,
            UUID mentorId
    );
}