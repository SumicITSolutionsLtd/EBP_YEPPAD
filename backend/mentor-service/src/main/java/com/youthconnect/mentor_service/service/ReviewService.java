package com.youthconnect.mentor_service.service;

import com.youthconnect.mentor_service.entity.Review;
import com.youthconnect.mentor_service.exception.SessionNotFoundException;
import com.youthconnect.mentor_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================================
 * REVIEW SERVICE
 * ============================================================================
 *
 * Business logic for review and rating management.
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
public class ReviewService {

    private final ReviewRepository reviewRepository;

    /**
     * Get all reviews for a mentor
     */
    @Cacheable(value = "reviews", key = "#mentorId")
    @Transactional(readOnly = true)
    public List<Review> getMentorReviews(Long mentorId) {
        log.debug("Fetching reviews for mentor ID: {}", mentorId);
        return reviewRepository.findByMentorIdOrderByCreatedAtDesc(mentorId);
    }

    /**
     * Get all reviews by a mentee
     */
    @Cacheable(value = "menteeReviews", key = "#menteeId")
    @Transactional(readOnly = true)
    public List<Review> getMenteeReviews(Long menteeId) {
        log.debug("Fetching reviews by mentee ID: {}", menteeId);
        return reviewRepository.findByMenteeIdOrderByCreatedAtDesc(menteeId);
    }

    /**
     * Get a specific review by ID (fixing the method name issue)
     */
    @Transactional(readOnly = true)
    public Review getReview(Long reviewId) {
        log.debug("Fetching review with ID: {}", reviewId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new SessionNotFoundException("Review not found with ID: " + reviewId));
    }

    /**
     * Get a specific review by ID (alternative method name)
     */
    @Transactional(readOnly = true)
    public Review getReviewById(Long reviewId) {
        return getReview(reviewId);
    }

    /**
     * Get review for a specific session
     */
    @Transactional(readOnly = true)
    public Review getReviewBySessionId(Long sessionId) {
        log.debug("Fetching review for session ID: {}", sessionId);
        return reviewRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Review not found for session ID: " + sessionId));
    }

    /**
     * Create a new review
     */
    @CacheEvict(value = {"reviews", "menteeReviews", "mentorStats"}, allEntries = true)
    public Review createReview(Review review) {
        log.info("Creating new review for mentor ID: {} by mentee ID: {}",
                review.getMentorId(), review.getMenteeId());

        // Validate rating
        validateRating(review.getRating());

        // Check if review already exists for this session
        if (review.getSessionId() != null) {
            reviewRepository.findBySessionId(review.getSessionId())
                    .ifPresent(existingReview -> {
                        throw new IllegalStateException("Review already exists for session ID: " + review.getSessionId());
                    });
        }

        review.setCreatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);

        log.info("Review created successfully with ID: {}", savedReview.getId());
        return savedReview;
    }

    /**
     * Update an existing review
     */
    @CacheEvict(value = {"reviews", "menteeReviews", "mentorStats"}, allEntries = true)
    public Review updateReview(Long reviewId, Review reviewUpdate) {
        log.info("Updating review with ID: {}", reviewId);

        Review existingReview = getReview(reviewId);

        // Validate new rating if provided
        if (reviewUpdate.getRating() != null) {
            validateRating(reviewUpdate.getRating());
            existingReview.setRating(reviewUpdate.getRating());
        }

        // Update comment if provided
        if (reviewUpdate.getComment() != null) {
            existingReview.setComment(reviewUpdate.getComment());
        }

        existingReview.setUpdatedAt(LocalDateTime.now());
        Review updatedReview = reviewRepository.save(existingReview);

        log.info("Review updated successfully with ID: {}", updatedReview.getId());
        return updatedReview;
    }

    /**
     * Delete a review
     */
    @CacheEvict(value = {"reviews", "menteeReviews", "mentorStats"}, allEntries = true)
    public void deleteReview(Long reviewId) {
        log.info("Deleting review with ID: {}", reviewId);

        if (!reviewRepository.existsById(reviewId)) {
            throw new SessionNotFoundException("Review not found with ID: " + reviewId);
        }

        reviewRepository.deleteById(reviewId);
        log.info("Review deleted successfully with ID: {}", reviewId);
    }

    /**
     * Calculate average rating for a mentor
     */
    @Cacheable(value = "mentorAverageRating", key = "#mentorId")
    @Transactional(readOnly = true)
    public Double calculateAverageRating(Long mentorId) {
        log.debug("Calculating average rating for mentor ID: {}", mentorId);

        List<Review> reviews = reviewRepository.findByMentorIdOrderByCreatedAtDesc(mentorId);

        if (reviews.isEmpty()) {
            log.debug("No reviews found for mentor ID: {}", mentorId);
            return 0.0;
        }

        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        log.debug("Average rating for mentor ID {}: {}", mentorId, average);
        return Math.round(average * 10.0) / 10.0; // Round to 1 decimal place
    }

    /**
     * Get review count for a mentor
     */
    @Cacheable(value = "mentorReviewCount", key = "#mentorId")
    @Transactional(readOnly = true)
    public Long getReviewCount(Long mentorId) {
        log.debug("Counting reviews for mentor ID: {}", mentorId);
        return reviewRepository.countByMentorId(mentorId);
    }

    /**
     * Get reviews with a specific rating for a mentor
     */
    @Transactional(readOnly = true)
    public List<Review> getReviewsByRating(Long mentorId, Integer rating) {
        log.debug("Fetching reviews with rating {} for mentor ID: {}", rating, mentorId);
        validateRating(rating);
        return reviewRepository.findByMentorIdAndRating(mentorId, rating);
    }

    /**
     * Get recent reviews for a mentor (last N reviews)
     */
    @Transactional(readOnly = true)
    public List<Review> getRecentReviews(Long mentorId, int limit) {
        log.debug("Fetching {} recent reviews for mentor ID: {}", limit, mentorId);

        List<Review> allReviews = reviewRepository.findByMentorIdOrderByCreatedAtDesc(mentorId);

        return allReviews.stream()
                .limit(limit)
                .toList();
    }

    /**
     * Check if a mentee has reviewed a specific session
     */
    @Transactional(readOnly = true)
    public boolean hasReviewedSession(Long menteeId, Long sessionId) {
        log.debug("Checking if mentee ID {} has reviewed session ID {}", menteeId, sessionId);
        return reviewRepository.existsByMenteeIdAndSessionId(menteeId, sessionId);
    }

    /**
     * Get rating distribution for a mentor (count of each rating 1-5)
     */
    @Transactional(readOnly = true)
    public RatingDistribution getRatingDistribution(Long mentorId) {
        log.debug("Calculating rating distribution for mentor ID: {}", mentorId);

        List<Review> reviews = reviewRepository.findByMentorIdOrderByCreatedAtDesc(mentorId);

        RatingDistribution distribution = new RatingDistribution();

        for (Review review : reviews) {
            switch (review.getRating()) {
                case 5 -> distribution.setFiveStars(distribution.getFiveStars() + 1);
                case 4 -> distribution.setFourStars(distribution.getFourStars() + 1);
                case 3 -> distribution.setThreeStars(distribution.getThreeStars() + 1);
                case 2 -> distribution.setTwoStars(distribution.getTwoStars() + 1);
                case 1 -> distribution.setOneStar(distribution.getOneStar() + 1);
            }
        }

        distribution.setTotalReviews(reviews.size());

        return distribution;
    }

    /**
     * Validate rating is within acceptable range (1-5)
     */
    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    /**
     * Inner class for rating distribution data
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RatingDistribution {
        private int fiveStars;
        private int fourStars;
        private int threeStars;
        private int twoStars;
        private int oneStar;
        private int totalReviews;
    }
}