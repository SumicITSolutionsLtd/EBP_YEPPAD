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

/**
 * ============================================================================
 * MENTOR AVAILABILITY REPOSITORY
 * ============================================================================
 *
 * Data access layer for mentor availability management.
 * Handles CRUD operations and complex queries for availability schedules.
 *
 * KEY OPERATIONS:
 * - Find availability by mentor and day
 * - Check for overlapping time slots
 * - Query active availability
 * - Conflict detection
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@Repository
public interface MentorAvailabilityRepository extends JpaRepository<MentorAvailability, Long> {

    /**
     * Find all availability slots for a mentor
     *
     * @param mentorId The mentor's user ID
     * @return List of availability slots
     */
    List<MentorAvailability> findByMentorId(Long mentorId);

    /**
     * Find active availability slots for a mentor
     *
     * @param mentorId The mentor's user ID
     * @return List of active availability slots
     */
    List<MentorAvailability> findByMentorIdAndIsActiveTrue(Long mentorId);

    /**
     * Find availability for specific mentor and day
     *
     * @param mentorId The mentor's user ID
     * @param dayOfWeek The day of week
     * @return List of availability slots for that day
     */
    List<MentorAvailability> findByMentorIdAndDayOfWeekAndIsActiveTrue(
            Long mentorId,
            DayOfWeek dayOfWeek
    );

    /**
     * Check if mentor has any availability on a specific day
     *
     * @param mentorId The mentor's user ID
     * @param dayOfWeek The day of week
     * @return true if availability exists
     */
    boolean existsByMentorIdAndDayOfWeekAndIsActiveTrue(
            Long mentorId,
            DayOfWeek dayOfWeek
    );

    /**
     * Find overlapping availability slots for conflict detection
     *
     * @param mentorId The mentor's user ID
     * @param dayOfWeek The day of week
     * @param startTime Slot start time
     * @param endTime Slot end time
     * @return List of conflicting slots
     */
    @Query("SELECT ma FROM MentorAvailability ma WHERE ma.mentorId = :mentorId " +
            "AND ma.dayOfWeek = :dayOfWeek " +
            "AND ma.isActive = true " +
            "AND ((ma.startTime < :endTime AND ma.endTime > :startTime))")
    List<MentorAvailability> findOverlappingSlots(
            @Param("mentorId") Long mentorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /**
     * Delete all availability for a mentor
     *
     * @param mentorId The mentor's user ID
     */
    void deleteByMentorId(Long mentorId);

    /**
     * Count total active availability slots for a mentor
     *
     * @param mentorId The mentor's user ID
     * @return Count of active slots
     */
    long countByMentorIdAndIsActiveTrue(Long mentorId);

    /**
     * Find availability slot by ID and mentor ID (for authorization)
     *
     * @param availabilityId The availability slot ID
     * @param mentorId The mentor's user ID
     * @return Optional availability slot
     */
    Optional<MentorAvailability> findByAvailabilityIdAndMentorId(
            Long availabilityId,
            Long mentorId
    );
}