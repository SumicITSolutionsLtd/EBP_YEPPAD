package com.youthconnect.mentor_service.service;

import com.youthconnect.mentor_service.client.NotificationServiceClient;
import com.youthconnect.mentor_service.client.UserServiceClient;
import com.youthconnect.mentor_service.config.ApplicationProperties;
import com.youthconnect.mentor_service.dto.*;
import com.youthconnect.mentor_service.dto.request.ReviewRequest;
import com.youthconnect.mentor_service.dto.request.SessionRequest;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * MENTORSHIP SERVICE - CORE BUSINESS LOGIC
 * ============================================================================
 *
 * Primary service for managing mentorship sessions, reviews, and mentor-mentee
 * relationships.
 *
 * RESPONSIBILITIES:
 * - Session booking and scheduling
 * - Session lifecycle management (schedule, confirm, complete, cancel)
 * - Review and rating system
 * - Session reminder coordination
 * - Mentor availability management
 * - Statistics and analytics
 *
 * INTEGRATION POINTS:
 * - user-service: Fetch mentor/mentee profiles
 * - notification-service: Send SMS/email notifications
 * - SessionReminderService: Schedule automated reminders
 *
 * PERFORMANCE:
 * - Caching for frequently accessed data
 * - Async processing for notifications
 * - Metrics tracking for monitoring
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MentorshipService {

    // ========================================================================
    // DEPENDENCIES
    // ========================================================================

    private final MentorshipSessionRepository sessionRepository;
    private final MentorAvailabilityRepository availabilityRepository;
    private final ReviewRepository reviewRepository;
    private final SessionReminderRepository reminderRepository;

    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationClient;

    private final SessionValidator sessionValidator;
    private final SessionReminderService reminderService;
    private final MentorMatchingService matchingService;

    private final ApplicationProperties applicationProperties;
    private final MeterRegistry meterRegistry;

    // ========================================================================
    // METRICS
    // ========================================================================

    private Counter sessionBookedCounter;
    private Counter sessionCompletedCounter;
    private Counter sessionCancelledCounter;
    private Timer sessionBookingTimer;

    /**
     * Initialize metrics on bean creation
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
    }

    // ========================================================================
    // SESSION BOOKING
    // ========================================================================

    /**
     * Book a new mentorship session
     *
     * WORKFLOW:
     * 1. Validate session request (time, availability, conflicts)
     * 2. Create session record in SCHEDULED status
     * 3. Create session reminders (24h, 1h before)
     * 4. Send confirmation notifications to mentor and mentee
     * 5. Update metrics and logs
     *
     * @param request Session booking details
     * @return Created session with details
     * @throws MentorNotAvailableException if mentor unavailable
     * @throws ValidationException if request invalid
     */
    public SessionResponse bookSession(SessionRequest request) {
        return sessionBookingTimer.recordCallable(() -> {
            log.info("Booking session: mentorId={}, menteeId={}, datetime={}",
                    request.getMentorId(), request.getMenteeId(), request.getSessionDatetime());

            // ----------------------------------------------------------------
            // STEP 1: COMPREHENSIVE VALIDATION
            // ----------------------------------------------------------------
            sessionValidator.validateSessionRequest(request);

            // ----------------------------------------------------------------
            // STEP 2: FETCH USER PROFILES
            // ----------------------------------------------------------------
            Map<String, Object> mentorProfile = userServiceClient.getMentorProfile(request.getMentorId());
            Map<String, Object> menteeProfile = userServiceClient.getYouthProfile(request.getMenteeId());

            if (mentorProfile == null || menteeProfile == null) {
                throw new IllegalArgumentException("Invalid mentor or mentee ID");
            }

            // ----------------------------------------------------------------
            // STEP 3: CREATE SESSION ENTITY
            // ----------------------------------------------------------------
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

            MentorshipSession savedSession = sessionRepository.save(session);
            log.info("Session created successfully with ID: {}", savedSession.getSessionId());

            // ----------------------------------------------------------------
            // STEP 4: CREATE AUTOMATED REMINDERS
            // ----------------------------------------------------------------
            try {
                reminderService.createRemindersForSession(savedSession.getSessionId());
                log.info("Reminders created for session: {}", savedSession.getSessionId());
            } catch (Exception e) {
                log.error("Failed to create reminders for session: {}", savedSession.getSessionId(), e);
                // Don't fail the booking if reminders fail
            }

            // ----------------------------------------------------------------
            // STEP 5: SEND CONFIRMATION NOTIFICATIONS (ASYNC)
            // ----------------------------------------------------------------
            sendSessionBookedNotificationsAsync(savedSession, mentorProfile, menteeProfile);

            // ----------------------------------------------------------------
            // STEP 6: UPDATE METRICS
            // ----------------------------------------------------------------
            sessionBookedCounter.increment();

            // ----------------------------------------------------------------
            // STEP 7: BUILD AND RETURN RESPONSE
            // ----------------------------------------------------------------
            return buildSessionResponse(savedSession, mentorProfile, menteeProfile);
        });
    }

    /**
     * Send session booked notifications asynchronously
     * Prevents blocking the main booking flow
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
            // Notification failure should not fail the booking
        }
    }

    // ========================================================================
    // SESSION RETRIEVAL
    // ========================================================================

    /**
     * Get session details by ID
     * Cached for 5 minutes to reduce database load
     *
     * @param sessionId The session identifier
     * @param userId The requesting user (for authorization)
     * @return Session details
     * @throws SessionNotFoundException if session not found
     * @throws SecurityException if user not authorized
     */
    @Cacheable(value = "sessions", key = "#sessionId")
    @Transactional(readOnly = true)
    public SessionResponse getSession(Long sessionId, Long userId) {
        log.debug("Fetching session: sessionId={}, requestingUserId={}", sessionId, userId);

        MentorshipSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // Authorization check: user must be mentor or mentee
        if (!session.getMentorId().equals(userId) && !session.getMenteeId().equals(userId)) {
            throw new SecurityException("User not authorized to view this session");
        }

        // Fetch profiles
        Map<String, Object> mentorProfile = userServiceClient.getMentorProfile(session.getMentorId());
        Map<String, Object> menteeProfile = userServiceClient.getYouthProfile(session.getMenteeId());

        return buildSessionResponse(session, mentorProfile, menteeProfile);
    }

    /**
     * Get all sessions for a user (as mentor or mentee)
     *
     * @param userId The user ID
     * @param pageable Pagination parameters
     * @return Page of sessions
     */
    @Transactional(readOnly = true)
    public Page<SessionResponse> getUserSessions(Long userId, Pageable pageable) {
        log.debug("Fetching sessions for user: {}", userId);

        Page<MentorshipSession> sessions = sessionRepository.findByMentorIdOrMenteeId(
                userId, userId, pageable);

        return sessions.map(session -> {
            try {
                Map<String, Object> mentorProfile = userServiceClient.getMentorProfile(session.getMentorId());
                Map<String, Object> menteeProfile = userServiceClient.getYouthProfile(session.getMenteeId());
                return buildSessionResponse(session, mentorProfile, menteeProfile);
            } catch (Exception e) {
                log.error("Error fetching profiles for session: {}", session.getSessionId(), e);
                return buildSessionResponse(session, null, null);
            }
        });
    }

    /**
     * Get upcoming sessions for a mentor
     *
     * @param mentorId The mentor ID
     * @param pageable Pagination parameters
     * @return Page of upcoming sessions
     */
    @Transactional(readOnly = true)
    public Page<SessionResponse> getUpcomingSessions(Long mentorId, Pageable pageable) {
        log.debug("Fetching upcoming sessions for mentor: {}", mentorId);

        Page<MentorshipSession> sessions = sessionRepository.findUpcomingSessionsByMentor(
                mentorId, LocalDateTime.now(), pageable);

        return sessions.map(session -> {
            Map<String, Object> mentorProfile = userServiceClient.getMentorProfile(session.getMentorId());
            Map<String, Object> menteeProfile = userServiceClient.getYouthProfile(session.getMenteeId());
            return buildSessionResponse(session, mentorProfile, menteeProfile);
        });
    }

    // ========================================================================
    // SESSION STATUS MANAGEMENT
    // ========================================================================

    /**
     * Complete a mentorship session
     * Only mentor or mentee can mark session as complete
     *
     * @param sessionId The session ID
     * @param userId The user completing the session
     * @param notes Optional session notes
     * @return Updated session
     * @throws SessionNotFoundException if session not found
     * @throws InvalidSessionStatusException if session cannot be completed
     */
    @CacheEvict(value = "sessions", key = "#sessionId")
    public SessionResponse completeSession(Long sessionId, Long userId, String notes) {
        log.info("Completing session: sessionId={}, userId={}", sessionId, userId);

        MentorshipSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // Validate user authorization
        boolean isMentor = session.getMentorId().equals(userId);
        boolean isMentee = session.getMenteeId().equals(userId);

        if (!isMentor && !isMentee) {
            throw new SecurityException("User not authorized to complete this session");
        }

        // Validate status transition
        sessionValidator.validateStatusTransition(session, MentorshipSession.SessionStatus.COMPLETED);

        // Update session
        session.setStatus(MentorshipSession.SessionStatus.COMPLETED);

        if (isMentor && notes != null) {
            session.setMentorNotes(notes);
        } else if (isMentee && notes != null) {
            session.setMenteeNotes(notes);
        }

        MentorshipSession updated = sessionRepository.save(session);

        // Send completion notifications
        sendSessionCompletedNotificationsAsync(updated);

        // Update metrics
        sessionCompletedCounter.increment();

        long durationMinutes = session.getDurationMinutes() != null ?
                session.getDurationMinutes() : 60;
        meterRegistry.timer("mentorship.session.duration")
                .record(durationMinutes, java.util.concurrent.TimeUnit.MINUTES);

        // Fetch profiles for response
        Map<String, Object> mentorProfile = userServiceClient.getMentorProfile(session.getMentorId());
        Map<String, Object> menteeProfile = userServiceClient.getYouthProfile(session.getMenteeId());

        return buildSessionResponse(updated, mentorProfile, menteeProfile);
    }

    /**
     * Cancel a mentorship session
     *
     * @param sessionId The session ID
     * @param userId The user cancelling
     * @param reason Cancellation reason
     * @return Updated session
     */
    @CacheEvict(value = "sessions", key = "#sessionId")
    public SessionResponse cancelSession(Long sessionId, Long userId, String reason) {
        log.info("Cancelling session: sessionId={}, userId={}, reason={}", sessionId, userId, reason);

        MentorshipSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // Validate authorization
        boolean isMentor = session.getMentorId().equals(userId);
        boolean isMentee = session.getMenteeId().equals(userId);

        if (!isMentor && !isMentee) {
            throw new SecurityException("User not authorized to cancel this session");
        }

        // Validate status
        if (!session.canBeCancelled()) {
            throw new InvalidSessionStatusException(
                    sessionId,
                    session.getStatus().name(),
                    "cancel"
            );
        }

        // Update session
        session.setStatus(MentorshipSession.SessionStatus.CANCELLED);
        MentorshipSession updated = sessionRepository.save(session);

        // Delete reminders
        reminderService.deleteRemindersForSession(sessionId);

        // Send cancellation notifications
        sendSessionCancelledNotificationsAsync(updated, isMentor ? "mentor" : "mentee", reason);

        // Update metrics
        sessionCancelledCounter.increment();

        // Fetch profiles
        Map<String, Object> mentorProfile = userServiceClient.getMentorProfile(session.getMentorId());
        Map<String, Object> menteeProfile = userServiceClient.getYouthProfile(session.getMenteeId());

        return buildSessionResponse(updated, mentorProfile, menteeProfile);
    }

    @Async("notificationExecutor")
    protected void sendSessionCompletedNotificationsAsync(MentorshipSession session) {
        try {
            notificationClient.sendSessionCompletedNotification(
                    session.getSessionId(),
                    session.getMentorId(),
                    session.getMenteeId()
            );
        } catch (Exception e) {
            log.error("Failed to send session completed notifications", e);
        }
    }

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
        } catch (Exception e) {
            log.error("Failed to send session cancelled notifications", e);
        }
    }

    // ========================================================================
    // REVIEW SYSTEM
    // ========================================================================

    /**
     * Submit a review for a completed session
     *
     * @param request Review details
     * @param reviewerId The user submitting review (mentee)
     * @return Created review
     */
    public Review submitReview(ReviewRequest request, Long reviewerId) {
        log.info("Submitting review: sessionId={}, reviewerId={}, rating={}",
                request.getSessionId(), reviewerId, request.getRating());

        // Validate review request
        if (request.getRating() < applicationProperties.getReviews().getMinRating() ||
                request.getRating() > applicationProperties.getReviews().getMaxRating()) {
            throw new IllegalArgumentException(
                    String.format("Rating must be between %d and %d",
                            applicationProperties.getReviews().getMinRating(),
                            applicationProperties.getReviews().getMaxRating())
            );
        }

        // Validate comment length
        if (request.getComment() != null) {
            int commentLength = request.getComment().length();
            if (commentLength < applicationProperties.getReviews().getMinCommentLength()) {
                throw new IllegalArgumentException(
                        String.format("Comment must be at least %d characters",
                                applicationProperties.getReviews().getMinCommentLength())
                );
            }
            if (commentLength > applicationProperties.getReviews().getMaxCommentLength()) {
                throw new IllegalArgumentException(
                        String.format("Comment cannot exceed %d characters",
                                applicationProperties.getReviews().getMaxCommentLength())
                );
            }
        }

        // Fetch session and validate
        MentorshipSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new SessionNotFoundException(request.getSessionId()));

        // Validate session is completed
        if (session.getStatus() != MentorshipSession.SessionStatus.COMPLETED) {
            throw new InvalidSessionStatusException(
                    session.getSessionId(),
                    session.getStatus().name(),
                    "submit review - session must be completed"
            );
        }

        // Validate reviewer is mentee
        if (!session.getMenteeId().equals(reviewerId)) {
            throw new SecurityException("Only mentee can review the session");
        }

        // Check for duplicate review
        boolean alreadyReviewed = reviewRepository.existsBySessionIdAndReviewerId(
                request.getSessionId(), reviewerId);

        if (alreadyReviewed) {
            throw new IllegalStateException("You have already reviewed this session");
        }

        // Create review
        Review review = Review.builder()
                .reviewerId(reviewerId)
                .revieweeId(session.getMentorId())
                .sessionId(request.getSessionId())
                .rating(request.getRating())
                .comment(request.getComment())
                .reviewType(Review.ReviewType.MENTOR_SESSION)
                .isApproved(true) // Auto-approve by default
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review submitted successfully: reviewId={}", saved.getReviewId());

        // Send notification to mentor
        sendReviewNotificationAsync(saved, session.getMentorId());

        // Update mentor average rating (cache eviction handled by service)
        evictMentorStatisticsCache(session.getMentorId());

        return saved;
    }

    @Async("notificationExecutor")
    protected void sendReviewNotificationAsync(Review review, Long mentorId) {
        try {
            notificationClient.sendReviewSubmittedNotification(
                    review.getReviewId(),
                    mentorId,
                    review.getRating()
            );
        } catch (Exception e) {
            log.error("Failed to send review notification", e);
        }
    }

    @CacheEvict(value = "statistics", key = "'mentor-' + #mentorId")
    protected void evictMentorStatisticsCache(Long mentorId) {
        log.debug("Evicted statistics cache for mentor: {}", mentorId);
    }

    // ========================================================================
    // MENTOR STATISTICS
    // ========================================================================

    /**
     * Get mentor statistics (cached)
     *
     * @param mentorId The mentor ID
     * @return Mentor statistics
     */
    @Cacheable(value = "statistics", key = "'mentor-' + #mentorId")
    @Transactional(readOnly = true)
    public MentorStatisticsDto getMentorStatistics(Long mentorId) {
        log.debug("Calculating statistics for mentor: {}", mentorId);

        // Get completed sessions count
        long completedSessions = sessionRepository.countByMentorIdAndStatus(
                mentorId, MentorshipSession.SessionStatus.COMPLETED);

        // Get total sessions
        long totalSessions = sessionRepository.countByMentorId(mentorId);

        // Get average rating
        Double averageRating = reviewRepository.getAverageRatingForMentor(mentorId);

        // Get total reviews
        long totalReviews = reviewRepository.countByRevieweeIdAndReviewType(
                mentorId, Review.ReviewType.MENTOR_SESSION);

        return MentorStatisticsDto.builder()
                .mentorId(mentorId)
                .completedSessions(completedSessions)
                .totalSessions(totalSessions)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews)
                .build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Build session response DTO from entity and profiles
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