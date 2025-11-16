package com.youthconnect.mentor_service.controller;

import com.youthconnect.mentor_service.entity.Review;
import com.youthconnect.mentor_service.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * ============================================================================
 * REVIEW CONTROLLER
 * ============================================================================
 *
 * REST controller for review and rating management.
 *
 * @author Douglas Kings Kato
 * @since 2025-11-07
 * ============================================================================
 */
@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reviews", description = "Review and rating management endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Get all reviews for a mentor (PAGINATED)
     */
    @GetMapping("/{mentorId}/reviews")
    @Operation(summary = "Get mentor reviews",
            description = "Retrieve all approved reviews for a mentor (paginated)")
    public ResponseEntity<Page<Review>> getMentorReviews(
            @PathVariable UUID mentorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("Fetching reviews for mentor: {} (page: {}, size: {})", mentorId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewService.getMentorReviews(mentorId, pageable);

        return ResponseEntity.ok(reviews);
    }

    /**
     * Get review details
     */
    @GetMapping("/reviews/{reviewId}")
    @Operation(summary = "Get review details")
    public ResponseEntity<Review> getReview(
            @PathVariable UUID reviewId
    ) {
        log.debug("Fetching review: {}", reviewId);
        Review review = reviewService.getReview(reviewId);
        return ResponseEntity.ok(review);
    }
}