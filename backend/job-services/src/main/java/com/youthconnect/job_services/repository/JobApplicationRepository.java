package com.youthconnect.job_services.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.youthconnect.job_services.entity.JobApplication;
import com.youthconnect.job_services.enums.ApplicationStatus;

import java.util.Optional;
import java.util.UUID;

/**
 * Job Application Repository - PostgreSQL with UUID Support
 *
 * All ID parameters use UUID.
 * Pagination support for all list methods.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    /**
     * Check if user has already applied to a job (both UUID)
     */
    boolean existsByJob_JobIdAndApplicantUserIdAndIsDeletedFalse(UUID jobId, UUID userId);

    /**
     * Find specific application by job and user (both UUID)
     */
    Optional<JobApplication> findByJob_JobIdAndApplicantUserIdAndIsDeletedFalse(UUID jobId, UUID userId);

    /**
     * Find all applications for a job (UUID)
     */
    Page<JobApplication> findByJob_JobIdAndIsDeletedFalse(UUID jobId, Pageable pageable);

    /**
     * Find all applications by a user (UUID)
     */
    Page<JobApplication> findByApplicantUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);

    /**
     * Find applications by status for a job (UUID)
     */
    Page<JobApplication> findByJob_JobIdAndStatusAndIsDeletedFalse(
            UUID jobId,
            ApplicationStatus status,
            Pageable pageable
    );

    /**
     * Count total applications for a job (UUID)
     */
    long countByJob_JobIdAndIsDeletedFalse(UUID jobId);

    /**
     * Count applications by user (UUID)
     */
    long countByApplicantUserIdAndIsDeletedFalse(UUID userId);

    /**
     * Find application by UUID (excluding deleted)
     */
    Optional<JobApplication> findByApplicationIdAndIsDeletedFalse(UUID applicationId);
}