package com.youthconnect.job_services.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.youthconnect.job_services.entity.Job;
import com.youthconnect.job_services.enums.JobStatus;
import com.youthconnect.job_services.enums.WorkMode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Job Repository - PostgreSQL with UUID Support
 *
 * All ID parameters use UUID instead of Long.
 * Pagination support for all list methods.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {

    // ============================================================================
    // BASIC CRUD OPERATIONS
    // ============================================================================

    /**
     * Find job by UUID (excluding deleted)
     */
    Optional<Job> findByJobIdAndIsDeletedFalse(UUID jobId);

    /**
     * Check if job exists by UUID
     */
    boolean existsByJobIdAndIsDeletedFalse(UUID jobId);

    // ============================================================================
    // PUBLISHED JOBS QUERIES
    // ============================================================================

    /**
     * Find published jobs that haven't expired
     */
    Page<Job> findByStatusAndExpiresAtAfterAndIsDeletedFalse(
            JobStatus status,
            LocalDateTime now,
            Pageable pageable
    );

    // ============================================================================
    // CATEGORY-BASED QUERIES
    // ============================================================================

    /**
     * Find jobs by category UUID
     */
    Page<Job> findByCategory_CategoryIdAndStatusAndExpiresAtAfterAndIsDeletedFalse(
            UUID categoryId,
            JobStatus status,
            LocalDateTime now,
            Pageable pageable
    );

    /**
     * Count active jobs by category UUID
     */
    long countByCategory_CategoryIdAndStatusAndIsDeletedFalse(UUID categoryId, JobStatus status);

    // ============================================================================
    // WORK MODE & LOCATION QUERIES
    // ============================================================================

    /**
     * Find jobs by work mode
     */
    Page<Job> findByWorkModeAndStatusAndExpiresAtAfterAndIsDeletedFalse(
            WorkMode workMode,
            JobStatus status,
            LocalDateTime now,
            Pageable pageable
    );

    /**
     * Find jobs by location (partial match)
     */
    Page<Job> findByLocationContainingIgnoreCaseAndStatusAndExpiresAtAfterAndIsDeletedFalse(
            String location,
            JobStatus status,
            LocalDateTime now,
            Pageable pageable
    );

    // ============================================================================
    // USER-SPECIFIC QUERIES
    // ============================================================================

    /**
     * Find all jobs posted by a user UUID
     */
    Page<Job> findByPostedByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);

    /**
     * Find all jobs by user (including deleted)
     */
    Page<Job> findByPostedByUserId(UUID userId, Pageable pageable);

    // ============================================================================
    // EXPIRATION & CLEANUP QUERIES
    // ============================================================================

    /**
     * Find expired jobs for cleanup
     */
    List<Job> findByStatusAndExpiresAtBeforeAndIsDeletedFalse(
            JobStatus status,
            LocalDateTime dateTime
    );

    /**
     * Find jobs expiring between dates (for reminders)
     */
    List<Job> findByStatusAndExpiresAtBetweenAndIsDeletedFalse(
            JobStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // ============================================================================
    // FEATURED & SPECIAL QUERIES
    // ============================================================================

    /**
     * Find featured jobs
     */
    Page<Job> findByIsFeaturedTrueAndStatusAndExpiresAtAfterAndIsDeletedFalse(
            JobStatus status,
            LocalDateTime now,
            Pageable pageable
    );

    // ============================================================================
    // SEARCH QUERIES
    // ============================================================================

    /**
     * Full-text search across job title, company, description
     */
    @Query("SELECT j FROM Job j WHERE j.status = :status " +
            "AND j.expiresAt > :now " +
            "AND j.isDeleted = false " +
            "AND (LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(j.jobDescription) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchJobs(
            @Param("keyword") String keyword,
            @Param("status") JobStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // ============================================================================
    // RECOMMENDATION QUERIES
    // ============================================================================

    /**
     * Find jobs excluding specific UUIDs (for recommendations)
     */
    Page<Job> findByStatusAndExpiresAtAfterAndJobIdNotInAndIsDeletedFalse(
            JobStatus status,
            LocalDateTime now,
            List<UUID> excludedJobIds,
            Pageable pageable
    );

    /**
     * Find all jobs by UUID list
     */
    List<Job> findAllByJobIdInAndIsDeletedFalse(List<UUID> jobIds);
}