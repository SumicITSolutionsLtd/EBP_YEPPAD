package com.youthconnect.content_service.repository;

import com.youthconnect.content_service.entity.ModuleProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ModuleProgress entity operations
 */
@Repository
public interface ModuleProgressRepository extends JpaRepository<ModuleProgress, Long> {

    /**
     * Find progress for a specific user and module
     */
    Optional<ModuleProgress> findByUserIdAndModuleId(Long userId, Long moduleId);

    /**
     * Find all progress records for a user
     */
    List<ModuleProgress> findByUserId(Long userId);

    /**
     * Find completed modules for a user
     */
    List<ModuleProgress> findByUserIdAndCompletedTrue(Long userId);

    /**
     * Find in-progress modules for a user
     */
    List<ModuleProgress> findByUserIdAndCompletedFalse(Long userId);
}