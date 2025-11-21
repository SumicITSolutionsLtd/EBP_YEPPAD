package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing MentorProfile entities.
 * Provides standard CRUD operations and allows for custom query methods.
 */
@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
    // Optional: Add custom query methods
    // List<MentorProfile> findByAreaOfExpertiseContaining(String expertise);

    // MentorProfileRepository
    Optional<MentorProfile> findByUser_Id(UUID userId);
}