package com.youthconnect.mentor_service.service;

import com.youthconnect.mentor_service.client.NotificationServiceClient;
import com.youthconnect.mentor_service.client.UserServiceClient;
import com.youthconnect.mentor_service.config.ApplicationProperties;
import com.youthconnect.mentor_service.dto.request.ReviewRequest;
import com.youthconnect.mentor_service.dto.request.SessionRequest;
import com.youthconnect.mentor_service.dto.response.MentorStatisticsDto;
import com.youthconnect.mentor_service.dto.response.SessionResponse;
import com.youthconnect.mentor_service.entity.*;
import com.youthconnect.mentor_service.exception.*;
import com.youthconnect.mentor_service.repository.*;
import com.youthconnect.mentor_service.validator.SessionValidator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * ============================================================================
 * MENTORSHIP SERVICE (FULLY FIXED)
 * ============================================================================
 *
 * Business logic for mentorship session management.
 *
 * FIXES APPLIED:
 * ✅ Proper exception handling for Timer.recordCallable()
 * ✅ UUID consistency throughout
 * ✅ Paginated responses
 * ✅ Metrics integration
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Full Compliance)
 * @since 2025-11-07
 * ============================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MentorshipService {

    private final MentorshipSessionRepository sessionRepository;
    private final MentorAvailabilityRepository availabilityRepository;
    private final ReviewRepository reviewRepository;
    private final SessionReminderRepository reminderRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationClient;
    private final SessionValidator sessionValidator;
    private final SessionReminderService reminderService;
    private final ApplicationProperties applicationProperties;
    private final MeterRegistry meterRegistry;

    private Counter sessionBookedCounter;
    private Counter sessionCompletedCounter;
    private Counter sessionCancelledCounter;
    private Timer sessionBookingTimer;

    /**
     * Initialize metrics on service startup
     */
    @jakarta.annotation.PostConstruct
    public void initMetrics() {
        sessionBookedCounter = Counter.builder("mentorship.sessions.booked")
                .description("Total sessions booked")
                .tag("service", "mentor-service")
                .register(meterRegistry);

        sessionCompletedCounter = Counter.builder("mentorship.sessions.completed")
                .description("Total sessions completed")
                .tag("service", "mentor-service")
                .register(meterRegistry);

        sessionCancelledCounter = Counter.builder("mentorship.sessions.cancelled")
                .description("Total sessions cancelled")
                .tag("service", "mentor-service")
                .register(meterRegistry);

        sessionBookingTimer = Timer.builder("mentorship.session.booking.time")
                .description("Time taken to book a session")
                .tag("service", "mentor-service")
                .register(meterRegistry);

        log.info("Mentorship service metrics initialized successfully");
    }

    /**
     * Book a new mentorship session
     *
     * ✅ FIXED: Proper exception handling for recordCallable()
     *
     * @param request Session booking request
     * @return SessionResponse DTO
     * @throws RuntimeException if booking fails
     */
    public SessionResponse bookSession(SessionRequest request) {
        try {
            // Record timing metrics using Timer
            return sessionBookingTimer.recordCallable(() -> {
                log.info("Booking session: mentorId={}, menteeId={}, datetime={}",
                        request.getMentorId(), request.getMenteeId(), request.getSessionDatetime());

                // Validate session request (time, availability, conflicts)
                sessionValidator.validateSessionRequest(request);

                // Note: UserServiceClient interaction omitted - can be added when needed
                Map<String, Object> mentorProfile = null;
                Map<String, Object> menteeProfile = null;

                // Build mentorship session entity
                MentorshipSession session = MentorshipSession.builder()
                        .mentorId(request.getMentorId())
                        .menteeId(request.getMenteeId())
                        .sessionDatetime(request.getSessionDatetime())
                        .durationMinutes(request.getDurationMinutes() != null ?
                                request.getDurationMinutes() :
                                applicationProperties.getMentorship().getDefaultSessionDuration())
                        .topic(request.getTopic())
                        .status(MentorshipSession.SessionStatus.SCHEDULED)
                        .build();

                // Save session to database
                MentorshipSession savedSession = sessionRepository.save(session);
                log.info("Session created successfully with ID: {}", savedSession.getSessionId());

                // Create automatic reminders (24h, 1h, 15min before session)
                try {
                    reminderService.createRemindersForSession(savedSession.getSessionId());
                } catch (Exception e) {
                    log.error("Failed to create reminders for session: {}", savedSession.getSessionId(), e);
                    // Don't fail the booking if reminders fail
                }

                // Send notifications asynchronously
                sendSessionBookedNotificationsAsync(savedSession, mentorProfile, menteeProfile);

                // Increment booking counter
                sessionBookedCounter.increment();

                // Return DTO response
                return buildSessionResponse(savedSession, mentorProfile, menteeProfile);
            });

        } catch (Exception e) {
            // Handle any exception from recordCallable
            log.error("Error booking session: {}", e.getMessage(), e);

            // Wrap checked exceptions in runtime exception
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Failed to book session: " + e.getMessage(), e);
        }
    }

    /**
     * Send session booked notifications asynchronously
     * Non-blocking - failures won't affect booking
     */
    @Async("notificationExecutor")
    protected void sendSessionBookedNotificationsAsync(
            MentorshipSession session,
            Map<String, Object> mentorProfile,
            Map<String, Object> menteeProfile) {
        try {
            notificationClient.sendSessionBookedNotification(
                    session.getSessionId(),
                    session.getMentorId(),
                    session.getMenteeId(),
                    session.getSessionDatetime(),
                    session.getTopic()
            );
            log.info("Session booked notifications sent for session: {}", session.getSessionId());
        } catch (Exception e) {
            log.error("Failed to send session booked notifications for session: {}",
                    session.getSessionId(), e);
        }
    }

    /**
     * Get session details by ID
     *
     * @param sessionId Session UUID
     * @param userId Requesting user UUID
     * @return SessionResponse DTO
     * @throws SessionNotFoundException if session not found
     * @throws SecurityException if user not authorized
     */
    @Cacheable(value = "sessions", key = "#sessionId")
    @Transactional(readOnly = true)
    public SessionResponse getSession(UUID sessionId, UUID userId) {
        log.debug("Fetching session: sessionId={}, requestingUserId={}", sessionId, userId);

        MentorshipSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

        // Verify user is either mentor or mentee
        if (!session.getMentorId().equals(userId) && !session.getMenteeId().equals(userId)) {
            throw new SecurityException("User not authorized to view this session");
        }

        Map<String, Object> mentorProfile = null;
        Map<String, Object> menteeProfile = null;

        return buildSessionResponse(session, mentorProfile, menteeProfile);
    }

    /**
     * Get all sessions for a user (paginated)
     *
     * @param userId User UUID (mentor or mentee)
     * @param pageable Pagination parameters
     * @return Page of SessionResponse DTOs
     */
    @Transactional(readOnly = true)
    public Page<SessionResponse> getUserSessions(UUID userId, Pageable pageable) {
        log.debug("Fetching sessions for user: {} (page: {}, size: {})",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<MentorshipSession> sessions = sessionRepository.findByMentorIdOrMenteeId(
                userId, userId, pageable);

        // Map to response DTOs
        return sessions.map(session -> buildSessionResponse(session, null, null));
    }

    /**
     * Complete a mentorship session
     *
     * @param sessionId Session UUID
     * @param userId User completing the session
     * @param notes Optional completion notes
     * @return Updated SessionResponse
     */
    @CacheEvict(value = "sessions", key = "#sessionId")
    public SessionResponse completeSession(UUID sessionId, UUID userId, String notes) {
        log.info("Completing session: sessionId={}, userId={}", sessionId, userId);

        MentorshipSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

        boolean isMentor = session.getMentorId().equals(userId);
        boolean isMentee = session.getMenteeId().equals(userId);

        if (!isMentor && !isMentee) {
            throw new SecurityException("User not authorized to complete this session");
        }

        // Validate status transition
        sessionValidator.validateStatusTransition(session, MentorshipSession.SessionStatus.COMPLETED);

        // Update session status
        session.setStatus(MentorshipSession.SessionStatus.COMPLETED);

        // Add notes based on who is completing
        if (isMentor && notes != null) {
            session.setMentorNotes(notes);
        } else if (isMentee && notes != null) {
            session.setMenteeNotes(notes);
        }

        MentorshipSession updated = sessionRepository.save(session);

        // Send completion notifications
        sendSessionCompletedNotificationsAsync(updated);
        sessionCompletedCounter.increment();

        return buildSessionResponse(updated, null, null);
    }

    /**
     * Cancel a mentorship session
     *
     * @param sessionId Session UUID
     * @param userId User cancelling the session
     * @param reason Cancellation reason
     * @return Updated SessionResponse
     */
    @CacheEvict(value = "sessions", key = "#sessionId")
    public SessionResponse cancelSession(UUID sessionId, UUID userId, String reason) {
        log.info("Cancelling session: sessionId={}, userId={}, reason={}", sessionId, userId, reason);

        MentorshipSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

        boolean isMentor = session.getMentorId().equals(userId);
        boolean isMentee = session.getMenteeId().equals(userId);

        if (!isMentor && !isMentee) {
            throw new SecurityException("User not authorized to cancel this session");
        }

        if (!session.canBeCancelled()) {
            throw new InvalidSessionStatusException(
                    sessionId,
                    session.getStatus().name(),
                    "cancel"
            );
        }

        session.setStatus(MentorshipSession.SessionStatus.CANCELLED);
        MentorshipSession updated = sessionRepository.save(session);

        // Delete scheduled reminders
        reminderService.deleteRemindersForSession(sessionId);

        // Send cancellation notifications
        sendSessionCancelledNotificationsAsync(updated, isMentor ? "mentor" : "mentee", reason);
        sessionCancelledCounter.increment();

        return buildSessionResponse(updated, null, null);
    }

    /**
     * Send session completed notifications asynchronously
     */
    @Async("notificationExecutor")
    protected void sendSessionCompletedNotificationsAsync(MentorshipSession session) {
        try {
            notificationClient.sendSessionCompletedNotification(
                    session.getSessionId(),
                    session.getMentorId(),
                    session.getMenteeId()
            );
            log.info("Session completed notifications sent for session: {}", session.getSessionId());
        } catch (Exception e) {
            log.error("Failed to send session completed notifications for session: {}",
                    session.getSessionId(), e);
        }
    }

    /**
     * Send session cancelled notifications asynchronously
     */
    @Async("notificationExecutor")
    protected void sendSessionCancelledNotificationsAsync(
            MentorshipSession session,
            String cancelledBy,
            String reason) {
        try {
            notificationClient.sendSessionCancelledNotification(
                    session.getSessionId(),
                    session.getMentorId(),
                    session.getMenteeId(),
                    cancelledBy,
                    reason
            );
            log.info("Session cancelled notifications sent for session: {}", session.getSessionId());
        } catch (Exception e) {
            log.error("Failed to send session cancelled notifications for session: {}",
                    session.getSessionId(), e);
        }
    }

    /**
     * Submit a review for a completed session
     *
     * @param request Review request
     * @param reviewerId Reviewer UUID (mentee)
     * @return Created review entity
     */
    public Review submitReview(ReviewRequest request, UUID reviewerId) {
        log.info("Submitting review: sessionId={}, reviewerId={}, rating={}",
                request.getSessionId(), reviewerId, request.getRating());

        // Validate rating range
        if (request.getRating() < applicationProperties.getReviews().getMinRating() ||
                request.getRating() > applicationProperties.getReviews().getMaxRating()) {
            throw new IllegalArgumentException(
                    String.format("Rating must be between %d and %d",
                            applicationProperties.getReviews().getMinRating(),
                            applicationProperties.getReviews().getMaxRating())
            );
        }

        // Convert session ID to UUID (handle if request uses different type)
        UUID sessionIdUuid;
        try {
            sessionIdUuid = UUID.fromString(request.getSessionId().toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid session ID format: " + request.getSessionId());
        }

        // Verify session exists and is completed
        MentorshipSession session = sessionRepository.findById(sessionIdUuid)
                .orElseThrow(() -> new SessionNotFoundException("Session not found"));

        if (session.getStatus() != MentorshipSession.SessionStatus.COMPLETED) {
            throw new InvalidSessionStatusException(
                    sessionIdUuid,
                    session.getStatus().name(),
                    "submit review - session must be completed"
            );
        }

        // Verify reviewer is the mentee
        if (!session.getMenteeId().equals(reviewerId)) {
            throw new SecurityException("Only mentee can review the session");
        }

        // Check for duplicate review
        boolean alreadyReviewed = reviewRepository.existsByReviewerIdAndSessionId(
                reviewerId, sessionIdUuid);

        if (alreadyReviewed) {
            throw new IllegalStateException("You have already reviewed this session");
        }

        // Create review entity
        Review review = Review.builder()
                .reviewerId(reviewerId)
                .revieweeId(session.getMentorId())
                .sessionId(sessionIdUuid)
                .rating(request.getRating())
                .comment(request.getComment())
                .reviewType(Review.ReviewType.MENTOR_SESSION)
                .isApproved(true)
                .build();

        Review saved = reviewRepository.save(review);

        // Invalidate mentor statistics cache
        evictMentorStatisticsCache(session.getMentorId());

        log.info("Review submitted successfully with ID: {}", saved.getReviewId());
        return saved;
    }

    /**
     * Evict mentor statistics cache after review submission
     */
    @CacheEvict(value = "statistics", key = "'mentor-' + #mentorId")
    protected void evictMentorStatisticsCache(UUID mentorId) {
        log.debug("Evicted statistics cache for mentor: {}", mentorId);
    }

    /**
     * Get mentor statistics (cached)
     *
     * @param mentorId Mentor UUID
     * @return MentorStatisticsDto
     */
    @Cacheable(value = "statistics", key = "'mentor-' + #mentorId")
    @Transactional(readOnly = true)
    public MentorStatisticsDto getMentorStatistics(UUID mentorId) {
        log.debug("Calculating statistics for mentor: {}", mentorId);

        long completedSessions = sessionRepository.countByMentorIdAndStatus(
                mentorId, MentorshipSession.SessionStatus.COMPLETED);

        long totalSessions = sessionRepository.countByMentorId(mentorId);

        Double averageRating = reviewRepository.getAverageRatingForMentor(mentorId);

        long totalReviews = reviewRepository.countByRevieweeIdAndReviewType(
                mentorId, Review.ReviewType.MENTOR_SESSION);

        MentorStatisticsDto stats = MentorStatisticsDto.builder()
                .mentorId(mentorId)
                .completedSessions(completedSessions)
                .totalSessions(totalSessions)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews)
                .build();

        // Calculate completion rate
        stats.calculateCompletionRate();

        return stats;
    }

    /**
     * Build session response DTO from entity
     *
     * @param session MentorshipSession entity
     * @param mentorProfile Mentor profile map (can be null)
     * @param menteeProfile Mentee profile map (can be null)
     * @return SessionResponse DTO
     */
    private SessionResponse buildSessionResponse(
            MentorshipSession session,
            Map<String, Object> mentorProfile,
            Map<String, Object> menteeProfile) {

        return SessionResponse.builder()
                .sessionId(session.getSessionId())
                .mentorId(session.getMentorId())
                .mentorName(mentorProfile != null ?
                        (String) mentorProfile.get("fullName") : "Unknown")
                .menteeId(session.getMenteeId())
                .menteeName(menteeProfile != null ?
                        (String) menteeProfile.get("fullName") : "Unknown")
                .sessionDatetime(session.getSessionDatetime())
                .durationMinutes(session.getDurationMinutes())
                .topic(session.getTopic())
                .status(session.getStatus().name())
                .mentorNotes(session.getMentorNotes())
                .menteeNotes(session.getMenteeNotes())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}