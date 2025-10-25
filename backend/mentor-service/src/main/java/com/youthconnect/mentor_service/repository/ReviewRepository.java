package com.youthconnect.mentor_service.repository;

import com.youthconnect.mentor_service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * REVIEW REPOSITORY
 * ============================================================================
 *
 * Data access layer for Review entity operations.
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find all reviews for a specific mentor, ordered by creation date descending
     *
     * @param mentorId the mentor's ID
     * @return list of reviews ordered by creation date (newest first)
     */
    List<Review> findByMentorIdOrderByCreatedAtDesc(Long mentorId);

    /**
     * Find all reviews by a specific mentee, ordered by creation date descending
     *
     * @param menteeId the mentee's ID
     * @return list of reviews ordered by creation date (newest first)
     */
    List<Review> findByMenteeIdOrderByCreatedAtDesc(Long menteeId);

    /**
     * Find review for a specific session
     *
     * @param sessionId the session ID
     * @return Optional containing the review if found
     */
    Optional<Review> findBySessionId(Long sessionId);

    /**
     * Count total number of reviews for a mentor
     *
     * @param mentorId the mentor's ID
     * @return count of reviews
     */
    Long countByMentorId(Long mentorId);

    /**
     * Find reviews for a mentor with a specific rating
     *
     * @param mentorId the mentor's ID
     * @param rating the rating value (1-5)
     * @return list of reviews with the specified rating
     */
    List<Review> findByMentorIdAndRating(Long mentorId, Integer rating);

    /**
     * Check if a mentee has reviewed a specific session
     *
     * @param menteeId the mentee's ID
     * @param sessionId the session ID
     * @return true if review exists, false otherwise
     */
    boolean existsByMenteeIdAndSessionId(Long menteeId, Long sessionId);
}