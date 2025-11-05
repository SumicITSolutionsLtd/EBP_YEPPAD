package com.youthconnect.job_services.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.youthconnect.job_services.entity.JobCategory;
import com.youthconnect.job_services.enums.JobStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Job Category Repository - PostgreSQL with UUID Support
 *
 * Primary key uses UUID.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, UUID> {

    /**
     * Find all active categories ordered by display order
     */
    List<JobCategory> findByIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc();

    /**
     * Find category by name (case-insensitive)
     */
    Optional<JobCategory> findByCategoryNameIgnoreCaseAndIsDeletedFalse(String categoryName);

    /**
     * Find category by UUID (excluding deleted)
     */
    Optional<JobCategory> findByCategoryIdAndIsDeletedFalse(UUID categoryId);

    /**
     * Check if category exists by UUID
     */
    boolean existsByCategoryIdAndIsDeletedFalse(UUID categoryId);

    /**
     * Count jobs in a category using custom query
     * (Workaround for complex relationship queries)
     */
    @Query("SELECT COUNT(j) FROM Job j WHERE j.category.categoryId = :categoryId " +
            "AND j.status = :status AND j.isDeleted = false")
    long countJobsInCategory(@Param("categoryId") UUID categoryId, @Param("status") JobStatus status);
}