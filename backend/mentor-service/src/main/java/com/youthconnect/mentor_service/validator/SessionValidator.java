package com.youthconnect.mentor_service.validator;

import com.youthconnect.mentor_service.dto.request.SessionRequest;
import com.youthconnect.mentor_service.entity.MentorAvailability;
import com.youthconnect.mentor_service.entity.MentorshipSession;
import com.youthconnect.mentor_service.exception.InvalidSessionStatusException;
import com.youthconnect.mentor_service.exception.MentorNotAvailableException;
import com.youthconnect.mentor_service.repository.MentorAvailabilityRepository;
import com.youthconnect.mentor_service.repository.MentorshipSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * ============================================================================
 * SESSION VALIDATOR
 * ============================================================================
 *
 * Validates mentorship session requests before booking.
 * Ensures:
 * - Session is during mentor's available hours
 * - No double-booking (scheduling conflicts)
 * - Session time is in the future
 * - Session duration is reasonable
 * - Minimum gap between sessions is maintained
 * - Maximum sessions per week not exceeded
 *
 * VALIDATION RULES:
 * 1. Basic Validation (DTO annotations)
 *    - Required fields present
 *    - Positive numbers for IDs and duration
 *    - Topic length within limits
 *
 * 2. Time Validation (This class)
 *    - Session in future (at least 2 hours advance)
 *    - Not more than 90 days in advance
 *    - Duration between 30-240 minutes
 *
 * 3. Availability Validation (This class)
 *    - Mentor has availability slot for day/time
 *    - No conflicting sessions
 *    - Minimum gap maintained (default 30 min)
 *    - Weekly session limit not exceeded
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionValidator {

    private final MentorAvailabilityRepository availabilityRepository;
    private final MentorshipSessionRepository sessionRepository;

    // Configurable validation parameters
    @Value("${app.mentorship.min-advance-hours:2}")
    private int minAdvanceHours;

    @Value("${app.mentorship.max-advance-days:90}")
    private int maxAdvanceDays;

    @Value("${app.mentorship.min-session-gap:30}")
    private int minSessionGapMinutes;

    @Value("${app.mentorship.max-sessions-per-week:10}")
    private int maxSessionsPerWeek;

    /**
     * Validate session request
     * Performs comprehensive validation of all session booking rules
     *
     * @param request The session booking request
     * @throws IllegalArgumentException if validation fails
     * @throws MentorNotAvailableException if mentor not available
     */
    public void validateSessionRequest(SessionRequest request) {
        log.debug("Validating session request for mentor {} on {}",
                request.getMentorId(), request.getSessionDatetime());

        // 1. Validate basic time constraints
        validateTimeConstraints(request);

        // 2. Validate mentor availability
        validateMentorAvailability(
                request.getMentorId(),
                request.getSessionDatetime(),
                request.getDurationMinutes()
        );

        // 3. Check for scheduling conflicts
        validateNoConflicts(
                request.getMentorId(),
                request.getSessionDatetime(),
                request.getDurationMinutes()
        );

        // 4. Validate weekly session limit
        validateWeeklyLimit(request.getMentorId(), request.getSessionDatetime());

        log.debug("Session request validation passed for mentor {}", request.getMentorId());
    }

    /**
     * Validate basic time constraints
     * Ensures session time is reasonable and within acceptable range
     *
     * @param request The session request
     * @throws IllegalArgumentException if time constraints violated
     */
    private void validateTimeConstraints(SessionRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionTime = request.getSessionDatetime();

        // Must be in future with minimum advance notice
        LocalDateTime minBookingTime = now.plusHours(minAdvanceHours);
        if (sessionTime.isBefore(minBookingTime)) {
            throw new IllegalArgumentException(String.format(
                    "Session must be booked at least %d hours in advance. " +
                            "Earliest available time: %s",
                    minAdvanceHours, minBookingTime
            ));
        }

        // Cannot be too far in advance
        LocalDateTime maxBookingTime = now.plusDays(maxAdvanceDays);
        if (sessionTime.isAfter(maxBookingTime)) {
            throw new IllegalArgumentException(String.format(
                    "Session cannot be scheduled more than %d days in advance. " +
                            "Latest booking date: %s",
                    maxAdvanceDays, maxBookingTime.toLocalDate()
            ));
        }

        // Validate duration
        Integer duration = request.getDurationMinutes();
        if (duration != null) {
            if (duration < 30) {
                throw new IllegalArgumentException(
                        "Session duration must be at least 30 minutes"
                );
            }
            if (duration > 240) {
                throw new IllegalArgumentException(
                        "Session duration cannot exceed 240 minutes (4 hours)"
                );
            }
        }

        // Validate topic
        if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
            throw new IllegalArgumentException("Session topic is required");
        }

        if (request.getTopic().length() < 5) {
            throw new IllegalArgumentException(
                    "Session topic must be at least 5 characters"
            );
        }

        if (request.getTopic().length() > 255) {
            throw new IllegalArgumentException(
                    "Session topic cannot exceed 255 characters"
            );
        }
    }

    /**
     * Validate mentor availability for requested time
     * Checks if mentor has availability slot matching requested day and time
     *
     * @param mentorId The mentor's user ID
     * @param sessionDatetime The requested session datetime
     * @param durationMinutes The session duration
     * @throws MentorNotAvailableException if mentor not available
     */
    private void validateMentorAvailability(
            Long mentorId,
            LocalDateTime sessionDatetime,
            Integer durationMinutes
    ) {
        DayOfWeek dayOfWeek = sessionDatetime.getDayOfWeek();
        LocalTime startTime = sessionDatetime.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(durationMinutes != null ? durationMinutes : 60);

        log.debug("Checking mentor {} availability for {} from {} to {}",
                mentorId, dayOfWeek, startTime, endTime);

        // Get mentor's availability for that day
        List<MentorAvailability> availabilitySlots = availabilityRepository
                .findByMentorIdAndDayOfWeekAndIsActiveTrue(mentorId, dayOfWeek);

        if (availabilitySlots.isEmpty()) {
            throw new MentorNotAvailableException(
                    mentorId,
                    sessionDatetime,
                    String.format("Mentor has no availability on %s", dayOfWeek)
            );
        }

        // Check if requested time falls within any availability slot
        boolean isAvailable = availabilitySlots.stream()
                .anyMatch(slot ->
                        !startTime.isBefore(slot.getStartTime()) &&
                                !endTime.isAfter(slot.getEndTime())
                );

        if (!isAvailable) {
            // Find closest available slot for helpful error message
            String availableTimes = availabilitySlots.stream()
                    .map(slot -> String.format("%s-%s", slot.getStartTime(), slot.getEndTime()))
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("None");

            throw new MentorNotAvailableException(
                    mentorId,
                    sessionDatetime,
                    String.format(
                            "Requested time %s-%s is outside mentor's available hours. " +
                                    "Available times on %s: %s",
                            startTime, endTime, dayOfWeek, availableTimes
                    )
            );
        }

        log.debug("Mentor {} is available at requested time", mentorId);
    }

    /**
     * Validate no scheduling conflicts
     * Ensures mentor doesn't have overlapping sessions
     * Also enforces minimum gap between sessions
     *
     * @param mentorId The mentor's user ID
     * @param sessionDatetime The requested session datetime
     * @param durationMinutes The session duration
     * @throws MentorNotAvailableException if conflicts exist
     */
    private void validateNoConflicts(
            Long mentorId,
            LocalDateTime sessionDatetime,
            Integer durationMinutes
    ) {
        int duration = durationMinutes != null ? durationMinutes : 60;

        // Add minimum gap to prevent back-to-back sessions
        LocalDateTime adjustedStart = sessionDatetime.minusMinutes(minSessionGapMinutes);
        LocalDateTime adjustedEnd = sessionDatetime.plusMinutes(duration + minSessionGapMinutes);

        log.debug("Checking for conflicts from {} to {} (includes {}-min gap)",
                adjustedStart, adjustedEnd, minSessionGapMinutes);

        // Find any overlapping sessions
        List<MentorshipSession> conflicts = sessionRepository.findConflictingSessions(
                mentorId,
                adjustedStart,
                adjustedEnd
        );

        if (!conflicts.isEmpty()) {
            MentorshipSession conflict = conflicts.get(0);
            throw MentorNotAvailableException.dueToConflict(
                    mentorId,
                    sessionDatetime,
                    conflict.getSessionId()
            );
        }

        log.debug("No scheduling conflicts found for mentor {}", mentorId);
    }

    /**
     * Validate weekly session limit
     * Ensures mentor doesn't exceed maximum sessions per week
     *
     * @param mentorId The mentor's user ID
     * @param sessionDatetime The requested session datetime
     * @throws MentorNotAvailableException if weekly limit exceeded
     */
    private void validateWeeklyLimit(Long mentorId, LocalDateTime sessionDatetime) {
        // Calculate week boundaries
        LocalDateTime weekStart = sessionDatetime
                .with(DayOfWeek.MONDAY)
                .truncatedTo(ChronoUnit.DAYS);
        LocalDateTime weekEnd = weekStart.plusDays(7);

        log.debug("Checking weekly session limit for mentor {} (week: {} to {})",
                mentorId, weekStart, weekEnd);

        // Count scheduled and in-progress sessions in that week
        long sessionsThisWeek = sessionRepository.countSessionsInDateRange(
                mentorId,
                weekStart,
                weekEnd,
                MentorshipSession.SessionStatus.SCHEDULED
        );

        log.debug("Mentor {} has {} sessions scheduled this week (limit: {})",
                mentorId, sessionsThisWeek, maxSessionsPerWeek);

        if (sessionsThisWeek >= maxSessionsPerWeek) {
            throw new MentorNotAvailableException(
                    mentorId,
                    sessionDatetime,
                    String.format(
                            "Mentor has reached maximum sessions for this week (%d/%d). " +
                                    "Please try a different week.",
                            sessionsThisWeek, maxSessionsPerWeek
                    )
            );
        }
    }

    /**
     * Validate session status transition
     * Ensures only valid status transitions are allowed
     *
     * @param session The session being updated
     * @param newStatus The new status being set
     * @throws InvalidSessionStatusException if transition not allowed
     */
    public void validateStatusTransition(
            MentorshipSession session,
            MentorshipSession.SessionStatus newStatus
    ) {
        MentorshipSession.SessionStatus currentStatus = session.getStatus();

        log.debug("Validating status transition from {} to {} for session {}",
                currentStatus, newStatus, session.getSessionId());

        // Define valid transitions
        boolean isValidTransition = switch (currentStatus) {
            case SCHEDULED -> newStatus == MentorshipSession.SessionStatus.IN_PROGRESS ||
                    newStatus == MentorshipSession.SessionStatus.CANCELLED ||
                    newStatus == MentorshipSession.SessionStatus.NO_SHOW;

            case IN_PROGRESS -> newStatus == MentorshipSession.SessionStatus.COMPLETED ||
                    newStatus == MentorshipSession.SessionStatus.CANCELLED;

            case COMPLETED, CANCELLED, NO_SHOW -> false; // Terminal states
        };

        if (!isValidTransition) {
            throw new InvalidSessionStatusException(
                    session.getSessionId(),
                    currentStatus.name(),
                    "transition to " + newStatus.name()
            );
        }

        log.debug("Status transition validated successfully");
    }
}