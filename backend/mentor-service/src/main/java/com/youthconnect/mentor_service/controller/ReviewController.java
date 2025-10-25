package com.youthconnect.mentor_service.controller;

import com.youthconnect.mentor_service.entity.Review;
import com.youthconnect.mentor_service.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================================
 * REVIEW CONTROLLER
 * ============================================================================
 *
 * REST controller for review and rating management.
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
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
     * Get all reviews for a mentor
     */
    @GetMapping("/{mentorId}/reviews")
    @Operation(summary = "Get mentor reviews",
            description = "Retrieve all approved reviews for a mentor")
    public ResponseEntity<List<Review>> getMentorReviews(
            @PathVariable Long mentorId
    ) {
        log.debug("Fetching reviews for mentor: {}", mentorId);
        List<Review> reviews = reviewService.getMentorReviews(mentorId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get review details
     */
    @GetMapping("/reviews/{reviewId}")
    @Operation(summary = "Get review details")
    public ResponseEntity<Review> getReview(
            @PathVariable Long reviewId
    ) {
        log.debug("Fetching review: {}", reviewId);
        Review review = reviewService.getReview(reviewId);
        return ResponseEntity.ok(review);
    }
}