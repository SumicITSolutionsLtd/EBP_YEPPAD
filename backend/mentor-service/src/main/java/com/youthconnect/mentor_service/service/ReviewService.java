package com.youthconnect.mentor_service.service;

import com.youthconnect.mentor_service.entity.Review;
import com.youthconnect.mentor_service.exception.SessionNotFoundException;
import com.youthconnect.mentor_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ============================================================================
 * REVIEW SERVICE
 * ============================================================================
 *
 * Business logic for review and rating management.
 *
 * @author Douglas Kings Kato
 * @since 2025-11-07
 * ============================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;

    /**
     * Get all reviews for a mentor (PAGINATED - COMPLIANCE)
     *
     * @param mentorId Mentor's UUID
     * @param pageable Pagination parameters
     * @return Paginated reviews
     */
    @Cacheable(value = "reviews", key = "#mentorId + '-' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<Review> getMentorReviews(UUID mentorId, Pageable pageable) {
        log.debug("Fetching reviews for mentor ID: {} (page: {})", mentorId, pageable.getPageNumber());
        return reviewRepository.findByMentorIdOrderByCreatedAtDesc(mentorId, pageable);
    }

    /**
     * Get all reviews by a mentee (PAGINATED - COMPLIANCE)
     *
     * @param menteeId Mentee's UUID
     * @param pageable Pagination parameters
     * @return Paginated reviews by mentee
     */
    @Cacheable(value = "menteeReviews", key = "#menteeId + '-' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<Review> getMenteeReviews(UUID menteeId, Pageable pageable) {
        log.debug("Fetching reviews by mentee ID: {} (page: {})", menteeId, pageable.getPageNumber());
        return reviewRepository.findByMenteeIdOrderByCreatedAtDesc(menteeId, pageable);
    }

    /**
     * Get a specific review by ID
     *
     * @param reviewId Review's UUID
     * @return Review entity
     * @throws SessionNotFoundException if review not found
     */
    @Transactional(readOnly = true)
    public Review getReview(UUID reviewId) {
        log.debug("Fetching review with ID: {}", reviewId);
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new SessionNotFoundException("Review not found with ID: " + reviewId));
    }

    /**
     * Get review for a specific session
     *
     * @param sessionId Session's UUID
     * @return Review for the session
     * @throws SessionNotFoundException if review not found
     */
    @Transactional(readOnly = true)
    public Review getReviewBySessionId(UUID sessionId) {
        log.debug("Fetching review for session ID: {}", sessionId);
        return reviewRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Review not found for session ID: " + sessionId));
    }

    /**
     * Create a new review
     *
     * @param review Review entity to create
     * @return Created review
     * @throws IllegalArgumentException if rating invalid or duplicate review
     */
    @CacheEvict(value = {"reviews", "menteeReviews", "mentorStats"}, allEntries = true)
    public Review createReview(Review review) {
        log.info("Creating new review for mentor ID: {} by mentee ID: {}",
                review.getMentorId(), review.getMenteeId());

        // Validate rating (1-5)
        validateRating(review.getRating());

        // Check if review already exists for this session
        if (review.getSessionId() != null) {
            boolean exists = reviewRepository.existsByReviewerIdAndSessionId(
                    review.getMenteeId(),
                    review.getSessionId()
            );

            if (exists) {
                throw new IllegalStateException(
                        "Review already exists for session ID: " + review.getSessionId()
                );
            }
        }

        // Set timestamps
        review.setCreatedAt(LocalDateTime.now());

        // Save review
        Review savedReview = reviewRepository.save(review);

        log.info("Review created successfully with ID: {}", savedReview.getReviewId());
        return savedReview;
    }

    /**
     * Update an existing review
     * @param reviewId Review's UUID
     * @param reviewUpdate Review with updated fields
     * @return Updated review
     * @throws SessionNotFoundException if review not found
     */
    @CacheEvict(value = {"reviews", "menteeReviews", "mentorStats"}, allEntries = true)
    public Review updateReview(UUID reviewId, Review reviewUpdate) {
        log.info("Updating review with ID: {}", reviewId);

        // Fetch existing review
        Review existingReview = getReview(reviewId);

        // Update rating if provided
        if (reviewUpdate.getRating() != null) {
            validateRating(reviewUpdate.getRating());
            existingReview.setRating(reviewUpdate.getRating());
        }

        // Update comment if provided
        if (reviewUpdate.getComment() != null) {
            existingReview.setComment(reviewUpdate.getComment());
        }

        // Set updated timestamp
        existingReview.setUpdatedAt(LocalDateTime.now());

        // Save updated review
        Review updatedReview = reviewRepository.save(existingReview);

        log.info("Review updated successfully with ID: {}", updatedReview.getReviewId());
        return updatedReview;
    }

    /**
     * Delete a review
     *
     * @param reviewId Review's UUID
     * @throws SessionNotFoundException if review not found
     */
    @CacheEvict(value = {"reviews", "menteeReviews", "mentorStats"}, allEntries = true)
    public void deleteReview(UUID reviewId) {
        log.info("Deleting review with ID: {}", reviewId);

        if (!reviewRepository.existsById(reviewId)) {
            throw new SessionNotFoundException("Review not found with ID: " + reviewId);
        }

        reviewRepository.deleteById(reviewId);
        log.info("Review deleted successfully with ID: {}", reviewId);
    }

    /**
     * Calculate average rating for a mentor
     *
     * @param mentorId Mentor's UUID
     * @return Average rating (0.0 if no reviews)
     */
    @Cacheable(value = "mentorAverageRating", key = "#mentorId")
    @Transactional(readOnly = true)
    public Double calculateAverageRating(UUID mentorId) {
        log.debug("Calculating average rating for mentor ID: {}", mentorId);

        Double averageRating = reviewRepository.getAverageRatingForMentor(mentorId);

        if (averageRating == null) {
            log.debug("No reviews found for mentor ID: {}", mentorId);
            return 0.0;
        }

        // Round to 1 decimal place
        double rounded = Math.round(averageRating * 10.0) / 10.0;

        log.debug("Average rating for mentor ID {}: {}", mentorId, rounded);
        return rounded;
    }

    /**
     * Get review count for a mentor
     *
     * @param mentorId Mentor's UUID
     * @return Total review count
     */
    @Cacheable(value = "mentorReviewCount", key = "#mentorId")
    @Transactional(readOnly = true)
    public Long getReviewCount(UUID mentorId) {
        log.debug("Counting reviews for mentor ID: {}", mentorId);
        return reviewRepository.countByMentorId(mentorId);
    }

    /**
     * Get reviews with a specific rating for a mentor (PAGINATED)
     *
     * @param mentorId Mentor's UUID
     * @param rating Rating value (1-5)
     * @param pageable Pagination parameters
     * @return Paginated reviews with specified rating
     */
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByRating(UUID mentorId, Integer rating, Pageable pageable) {
        log.debug("Fetching reviews with rating {} for mentor ID: {}", rating, mentorId);

        validateRating(rating);

        return reviewRepository.findByMentorIdAndRating(mentorId, rating, pageable);
    }

    /**
     * Check if a mentee has reviewed a specific session
     *
     * @param menteeId Mentee's UUID
     * @param sessionId Session's UUID
     * @return true if review exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean hasReviewedSession(UUID menteeId, UUID sessionId) {
        log.debug("Checking if mentee ID {} has reviewed session ID {}", menteeId, sessionId);
        return reviewRepository.existsByMenteeIdAndSessionId(menteeId, sessionId);
    }

    /**
     * Get rating distribution for a mentor
     *
     * @param mentorId Mentor's UUID
     * @return Rating distribution summary
     */
    @Transactional(readOnly = true)
    public RatingDistribution getRatingDistribution(UUID mentorId) {
        log.debug("Calculating rating distribution for mentor ID: {}", mentorId);

        RatingDistribution distribution = new RatingDistribution();

        // Count reviews for each rating (1-5)
        for (int rating = 1; rating <= 5; rating++) {
            Long count = reviewRepository.countByRevieweeIdAndReviewType(
                    mentorId,
                    Review.ReviewType.MENTOR_SESSION
            );

            switch (rating) {
                case 5 -> distribution.setFiveStars(count != null ? count.intValue() : 0);
                case 4 -> distribution.setFourStars(count != null ? count.intValue() : 0);
                case 3 -> distribution.setThreeStars(count != null ? count.intValue() : 0);
                case 2 -> distribution.setTwoStars(count != null ? count.intValue() : 0);
                case 1 -> distribution.setOneStar(count != null ? count.intValue() : 0);
            }
        }

        // Calculate total
        int total = distribution.getFiveStars() + distribution.getFourStars() +
                distribution.getThreeStars() + distribution.getTwoStars() +
                distribution.getOneStar();
        distribution.setTotalReviews(total);

        return distribution;
    }

    /**
     * Validate rating is within acceptable range (1-5)
     *
     * @param rating Rating value to validate
     * @throws IllegalArgumentException if rating invalid
     */
    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    /**
     * Inner class for rating distribution data
     * Provides summary of ratings across all levels
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

        /**
         * Calculate percentage of total for a specific rating
         *
         * @param count Count for specific rating
         * @return Percentage (0-100)
         */
        public double getPercentage(int count) {
            if (totalReviews == 0) return 0.0;
            return Math.round((count / (double) totalReviews) * 100.0 * 10.0) / 10.0;
        }
    }
}