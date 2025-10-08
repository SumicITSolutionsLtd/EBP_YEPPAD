package com.youthconnect.mentor_service.repository;

import com.youthconnect.mentor_service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Custom queries can be added later if needed (e.g., find all reviews for a specific mentor)
}