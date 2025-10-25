package com.youthconnect.mentor_service.controller;

import com.youthconnect.mentor_service.dto.request.ReviewRequest;
import com.youthconnect.mentor_service.dto.request.SessionRequest;
import com.youthconnect.mentor_service.dto.response.SessionResponse;
import com.youthconnect.mentor_service.entity.Review;
import com.youthconnect.mentor_service.service.MentorshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================================
 * MENTORSHIP CONTROLLER
 * ============================================================================
 *
 * REST controller for mentorship session management.
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@RestController
@RequestMapping("/api/mentorship")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mentorship", description = "Mentorship session management endpoints")
public class MentorshipController {

    private final MentorshipService mentorshipService;

    /**
     * Book a new mentorship session
     */
    @PostMapping("/sessions")
    @PreAuthorize("hasRole('YOUTH')")
    @Operation(summary = "Book mentorship session",
            description = "Book a new mentorship session with a mentor")
    public ResponseEntity<SessionResponse> bookSession(
            @Valid @RequestBody SessionRequest request
    ) {
        log.info("Booking session request: mentorId={}, menteeId={}",
                request.getMentorId(), request.getMenteeId());
        SessionResponse session = mentorshipService.bookSession(request);
        return new ResponseEntity<>(session, HttpStatus.CREATED);
    }

    /**
     * Get user's sessions (as mentor or mentee)
     */
    @GetMapping("/sessions/my-sessions")
    @Operation(summary = "Get my sessions",
            description = "Get all sessions for the authenticated user")
    public ResponseEntity<Page<SessionResponse>> getMySessions(
            @RequestHeader("X-User-Id") Long userId,
            Pageable pageable
    ) {
        log.debug("Fetching sessions for user: {}", userId);
        Page<SessionResponse> sessions = mentorshipService.getUserSessions(userId, pageable);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get session details
     */
    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get session details")
    public ResponseEntity<SessionResponse> getSession(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        log.debug("Fetching session: {}", sessionId);
        SessionResponse session = mentorshipService.getSession(sessionId, userId);
        return ResponseEntity.ok(session);
    }

    /**
     * Complete a session
     */
    @PutMapping("/sessions/{sessionId}/complete")
    @Operation(summary = "Complete session")
    public ResponseEntity<SessionResponse> completeSession(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String notes
    ) {
        log.info("Completing session: {}", sessionId);
        SessionResponse session = mentorshipService.completeSession(sessionId, userId, notes);
        return ResponseEntity.ok(session);
    }

    /**
     * Cancel a session
     */
    @PutMapping("/sessions/{sessionId}/cancel")
    @Operation(summary = "Cancel session")
    public ResponseEntity<SessionResponse> cancelSession(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String reason
    ) {
        log.info("Cancelling session: {}", sessionId);
        SessionResponse session = mentorshipService.cancelSession(sessionId, userId, reason);
        return ResponseEntity.ok(session);
    }

    /**
     * Submit a review for a completed session
     */
    @PostMapping("/reviews")
    @PreAuthorize("hasRole('YOUTH')")
    @Operation(summary = "Submit session review",
            description = "Submit a review and rating for a completed session")
    public ResponseEntity<Review> submitReview(
            @Valid @RequestBody ReviewRequest request,
            @RequestHeader("X-User-Id") Long reviewerId
    ) {
        log.info("Submitting review for session: {}", request.getSessionId());
        Review review = mentorshipService.submitReview(request, reviewerId);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }
}