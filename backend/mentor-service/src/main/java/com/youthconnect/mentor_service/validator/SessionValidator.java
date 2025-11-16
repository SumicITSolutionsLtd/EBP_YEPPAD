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
import java.util.UUID;

/**
 * ============================================================================
 * SESSION VALIDATOR
 * ============================================================================
 * @author Douglas Kings Kato
 * @since 2025-11-07
 * ============================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionValidator {

    private final MentorAvailabilityRepository availabilityRepository;
    private final MentorshipSessionRepository sessionRepository;

    @Value("${app.mentorship.min-advance-hours:2}")
    private int minAdvanceHours;

    @Value("${app.mentorship.max-advance-days:90}")
    private int maxAdvanceDays;

    @Value("${app.mentorship.min-session-gap:30}")
    private int minSessionGapMinutes;

    @Value("${app.mentorship.max-sessions-per-week:10}")
    private int maxSessionsPerWeek;

    public void validateSessionRequest(SessionRequest request) {
        log.debug("Validating session request for mentor {} on {}",
                request.getMentorId(), request.getSessionDatetime());

        validateTimeConstraints(request);
        validateMentorAvailability(
                request.getMentorId(),
                request.getSessionDatetime(),
                request.getDurationMinutes()
        );
        validateNoConflicts(
                request.getMentorId(),
                request.getSessionDatetime(),
                request.getDurationMinutes()
        );
        validateWeeklyLimit(request.getMentorId(), request.getSessionDatetime());
    }

    private void validateTimeConstraints(SessionRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionTime = request.getSessionDatetime();

        LocalDateTime minBookingTime = now.plusHours(minAdvanceHours);
        if (sessionTime.isBefore(minBookingTime)) {
            throw new IllegalArgumentException(String.format(
                    "Session must be booked at least %d hours in advance. " +
                            "Earliest available time: %s",
                    minAdvanceHours, minBookingTime
            ));
        }

        LocalDateTime maxBookingTime = now.plusDays(maxAdvanceDays);
        if (sessionTime.isAfter(maxBookingTime)) {
            throw new IllegalArgumentException(String.format(
                    "Session cannot be scheduled more than %d days in advance. " +
                            "Latest booking date: %s",
                    maxAdvanceDays, maxBookingTime.toLocalDate()
            ));
        }

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
    }

    private void validateMentorAvailability(
            UUID mentorId,
            LocalDateTime sessionDatetime,
            Integer durationMinutes
    ) {
        DayOfWeek dayOfWeek = sessionDatetime.getDayOfWeek();
        LocalTime startTime = sessionDatetime.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(durationMinutes != null ? durationMinutes : 60);

        List<MentorAvailability> availabilitySlots = availabilityRepository
                .findByMentorIdAndDayOfWeekAndIsActiveTrue(mentorId, dayOfWeek);

        if (availabilitySlots.isEmpty()) {
            throw new MentorNotAvailableException(
                    mentorId,
                    sessionDatetime,
                    String.format("Mentor has no availability on %s", dayOfWeek)
            );
        }

        boolean isAvailable = availabilitySlots.stream()
                .anyMatch(slot ->
                        !startTime.isBefore(slot.getStartTime()) &&
                                !endTime.isAfter(slot.getEndTime())
                );

        if (!isAvailable) {
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
    }

    private void validateNoConflicts(
            UUID mentorId,
            LocalDateTime sessionDatetime,
            Integer durationMinutes
    ) {
        int duration = durationMinutes != null ? durationMinutes : 60;

        LocalDateTime adjustedStart = sessionDatetime.minusMinutes(minSessionGapMinutes);
        LocalDateTime adjustedEnd = sessionDatetime.plusMinutes(duration + minSessionGapMinutes);

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
    }

    private void validateWeeklyLimit(UUID mentorId, LocalDateTime sessionDatetime) {
        LocalDateTime weekStart = sessionDatetime
                .with(DayOfWeek.MONDAY)
                .truncatedTo(ChronoUnit.DAYS);
        LocalDateTime weekEnd = weekStart.plusDays(7);

        long sessionsThisWeek = sessionRepository.countSessionsInDateRange(
                mentorId,
                weekStart,
                weekEnd,
                MentorshipSession.SessionStatus.SCHEDULED
        );

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

    public void validateStatusTransition(
            MentorshipSession session,
            MentorshipSession.SessionStatus newStatus
    ) {
        MentorshipSession.SessionStatus currentStatus = session.getStatus();

        boolean isValidTransition = switch (currentStatus) {
            case SCHEDULED -> newStatus == MentorshipSession.SessionStatus.IN_PROGRESS ||
                    newStatus == MentorshipSession.SessionStatus.CANCELLED ||
                    newStatus == MentorshipSession.SessionStatus.NO_SHOW;

            case IN_PROGRESS -> newStatus == MentorshipSession.SessionStatus.COMPLETED ||
                    newStatus == MentorshipSession.SessionStatus.CANCELLED;

            case COMPLETED, CANCELLED, NO_SHOW -> false;
        };

        if (!isValidTransition) {
            throw new InvalidSessionStatusException(
                    session.getSessionId(),
                    currentStatus.name(),
                    "transition to " + newStatus.name()
            );
        }
    }
}