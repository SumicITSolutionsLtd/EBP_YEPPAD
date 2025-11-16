package com.youthconnect.mentor_service.repository;

import com.youthconnect.mentor_service.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * ============================================================================
 * REVIEW REPOSITORY (FIXED - COMPLIANCE VERSION)
 * ============================================================================
 *
 * Data access layer for Review entity operations.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Compliance Update)
 * @since 2025-11-07
 * ============================================================================
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // ========================================================================
    // PAGINATED QUERY METHODS (COMPLIANCE REQUIREMENT)
    // ========================================================================

    /**
     * Find reviews by reviewee (mentor) ID with pagination

     * @param revieweeId Mentor's UUID
     * @param pageable Pagination parameters
     * @return Paginated reviews ordered by creation date
     */
    Page<Review> findByRevieweeIdOrderByCreatedAtDesc(UUID revieweeId, Pageable pageable);

    /**
     * Compatibility method: Use mentor terminology
     */
    default Page<Review> findByMentorIdOrderByCreatedAtDesc(UUID mentorId, Pageable pageable) {
        return findByRevieweeIdOrderByCreatedAtDesc(mentorId, pageable);
    }

    /**
     * Find reviews by reviewer (mentee) ID with pagination

     * @param reviewerId Mentee's UUID
     * @param pageable Pagination parameters
     * @return Paginated reviews by the mentee
     */
    Page<Review> findByReviewerIdOrderByCreatedAtDesc(UUID reviewerId, Pageable pageable);

    /**
     * Compatibility method: Use mentee terminology
     */
    default Page<Review> findByMenteeIdOrderByCreatedAtDesc(UUID menteeId, Pageable pageable) {
        return findByReviewerIdOrderByCreatedAtDesc(menteeId, pageable);
    }

    /**
     * Find reviews by mentor ID and rating with pagination

     * @param revieweeId Mentor's UUID
     * @param rating Rating value (1-5)
     * @param pageable Pagination parameters
     * @return Paginated reviews with specified rating
     */
    Page<Review> findByRevieweeIdAndRating(UUID revieweeId, Integer rating, Pageable pageable);

    /**
     * Compatibility method
     */
    default Page<Review> findByMentorIdAndRating(UUID mentorId, Integer rating, Pageable pageable) {
        return findByRevieweeIdAndRating(mentorId, rating, pageable);
    }

    // ========================================================================
    // NON-PAGINATED QUERY METHODS (FOR SPECIFIC USE CASES)
    // ========================================================================

    /**
     * Find review by session ID (single result expected)

     * @param sessionId Session's UUID
     * @return Optional review for the session
     */
    Optional<Review> findBySessionId(UUID sessionId);

    /**
     * Count reviews by reviewee (mentor) ID

     * @param revieweeId Mentor's UUID
     * @return Total review count
     */
    Long countByRevieweeId(UUID revieweeId);

    /**
     * Compatibility method
     */
    default Long countByMentorId(UUID mentorId) {
        return countByRevieweeId(mentorId);
    }

    /**
     * Check if review exists by reviewer and session

     * @param reviewerId Mentee's UUID
     * @param sessionId Session's UUID
     * @return true if review exists
     */
    boolean existsByReviewerIdAndSessionId(UUID reviewerId, UUID sessionId);

    /**
     * Compatibility method
     */
    default boolean existsByMenteeIdAndSessionId(UUID menteeId, UUID sessionId) {
        return existsByReviewerIdAndSessionId(menteeId, sessionId);
    }

    // ========================================================================
    // AGGREGATION QUERIES
    // ========================================================================

    /**
     * Get average rating for a mentor

     * @param revieweeId Mentor's UUID
     * @return Average rating or null if no reviews
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.revieweeId = :revieweeId " +
            "AND r.reviewType = 'MENTOR_SESSION' AND r.isApproved = true")
    Double getAverageRatingForMentor(@Param("revieweeId") UUID revieweeId);

    /**
     * Count reviews by reviewee ID and review type

     * @param revieweeId Mentor's UUID
     * @param reviewType Type of review
     * @return Count of reviews
     */
    Long countByRevieweeIdAndReviewType(UUID revieweeId, Review.ReviewType reviewType);
}