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
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * ============================================================================
 * MENTORSHIP CONTROLLER (FIXED - COMPLIANCE VERSION)
 * ============================================================================
 *
 * REST controller for mentorship session management.
 *
 * COMPLIANCE FIXES:
 * ✅ Returns DTOs/Entities directly (not ResponseEntity)
 * ✅ Uses UUID for all identifiers
 * ✅ Implements pagination for all list endpoints
 * ✅ Includes health check endpoint
 * ✅ Swagger documentation enabled
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Compliance Update)
 * @since 2025-11-07
 * ============================================================================
 */
@RestController
@RequestMapping("/api/mentorship")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mentorship", description = "Mentorship session management endpoints")
public class MentorshipController {

    private final MentorshipService mentorshipService;

    // ========================================================================
    // HEALTH CHECK ENDPOINT (COMPLIANCE REQUIREMENT)
    // ========================================================================

    /**
     * Health check endpoint for service monitoring
     * Public endpoint - no authentication required
     *
     * @return Health status string
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if mentorship service is running")
    public String healthCheck() {
        log.debug("Health check requested");
        return "Mentorship Service is UP";
    }

    // ========================================================================
    // SESSION MANAGEMENT ENDPOINTS
    // ========================================================================

    /**
     * Book a new mentorship session
     *
     * ✅ COMPLIANCE: Returns DTO directly (not ResponseEntity)
     * ✅ COMPLIANCE: Uses UUID for IDs
     *
     * @param request Session booking request with UUID IDs
     * @return SessionResponse DTO (not wrapped in ResponseEntity)
     */
    @PostMapping("/sessions")
    @PreAuthorize("hasRole('YOUTH')")
    @Operation(summary = "Book mentorship session",
            description = "Book a new mentorship session with a mentor")
    public SessionResponse bookSession(@Valid @RequestBody SessionRequest request) {
        log.info("Booking session request: mentorId={}, menteeId={}, datetime={}",
                request.getMentorId(), request.getMenteeId(), request.getSessionDatetime());

        // Return DTO directly - no ResponseEntity wrapper
        return mentorshipService.bookSession(request);
    }

    /**
     * Get user's sessions (as mentor or mentee)
     *
     * ✅ COMPLIANCE: Implements pagination (returns Page<DTO>)
     * ✅ COMPLIANCE: Uses UUID for user identification
     * ✅ COMPLIANCE: Returns DTO directly
     *
     * @param userId User's UUID from authentication header
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @return Paginated list of sessions
     */
    @GetMapping("/sessions/my-sessions")
    @Operation(summary = "Get my sessions",
            description = "Get all sessions for the authenticated user (paginated)")
    public Page<SessionResponse> getMySessions(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("Fetching sessions for user: {} (page: {}, size: {})", userId, page, size);

        // Create pageable and return paginated results directly
        Pageable pageable = PageRequest.of(page, size);
        return mentorshipService.getUserSessions(userId, pageable);
    }

    /**
     * Get session details by ID
     *
     * ✅ COMPLIANCE: Uses UUID for session identification
     * ✅ COMPLIANCE: Returns DTO directly
     *
     * @param sessionId Session's UUID
     * @param userId Requesting user's UUID
     * @return Session details DTO
     */
    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get session details")
    public SessionResponse getSession(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        log.debug("Fetching session: {}", sessionId);
        return mentorshipService.getSession(sessionId, userId);
    }

    /**
     * Complete a session
     *
     * ✅ COMPLIANCE: Uses UUID, returns DTO
     *
     * @param sessionId Session's UUID
     * @param userId User performing action (UUID)
     * @param notes Optional completion notes
     * @return Updated session DTO
     */
    @PutMapping("/sessions/{sessionId}/complete")
    @Operation(summary = "Complete session")
    public SessionResponse completeSession(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) String notes
    ) {
        log.info("Completing session: {}", sessionId);
        return mentorshipService.completeSession(sessionId, userId, notes);
    }

    /**
     * Cancel a session
     *
     * ✅ COMPLIANCE: Uses UUID, returns DTO
     *
     * @param sessionId Session's UUID
     * @param userId User performing action (UUID)
     * @param reason Optional cancellation reason
     * @return Updated session DTO
     */
    @PutMapping("/sessions/{sessionId}/cancel")
    @Operation(summary = "Cancel session")
    public SessionResponse cancelSession(
            @PathVariable UUID sessionId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) String reason
    ) {
        log.info("Cancelling session: {}", sessionId);
        return mentorshipService.cancelSession(sessionId, userId, reason);
    }

    // ========================================================================
    // REVIEW MANAGEMENT ENDPOINTS
    // ========================================================================

    /**
     * Submit a review for a completed session
     *
     * ✅ COMPLIANCE: Returns entity directly
     * ✅ COMPLIANCE: Uses UUID for reviewer identification
     *
     * @param request Review submission request
     * @param reviewerId Reviewer's UUID from authentication
     * @return Created review entity
     */
    @PostMapping("/reviews")
    @PreAuthorize("hasRole('YOUTH')")
    @Operation(summary = "Submit session review",
            description = "Submit a review and rating for a completed session")
    public Review submitReview(
            @Valid @RequestBody ReviewRequest request,
            @RequestHeader("X-User-Id") UUID reviewerId
    ) {
        log.info("Submitting review for session: {}", request.getSessionId());
        return mentorshipService.submitReview(request, reviewerId);
    }
}