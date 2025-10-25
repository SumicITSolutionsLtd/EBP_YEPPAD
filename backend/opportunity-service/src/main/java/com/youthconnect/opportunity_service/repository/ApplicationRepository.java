package com.youthconnect.opportunity_service.repository;

import com.youthconnect.opportunity_service.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Application entity with custom query methods
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Find all applications for a specific opportunity
     */
    List<Application> findByOpportunityId(Long opportunityId);

    /**
     * Find all applications by a specific user
     */
    List<Application> findByApplicantId(Long applicantId);

    /**
     * Find applications by status
     */
    List<Application> findByStatus(Application.Status status);

    /**
     * Check if user has already applied to an opportunity (prevent duplicates)
     */
    boolean existsByOpportunityIdAndApplicantId(Long opportunityId, Long applicantId);

    /**
     * Find application by opportunity and applicant
     */
    Optional<Application> findByOpportunityIdAndApplicantId(Long opportunityId, Long applicantId);

    /**
     * Count applications for an opportunity
     */
    long countByOpportunityId(Long opportunityId);

    /**
     * Count applications by user submitted today (rate limiting)
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.applicantId = :userId " +
            "AND a.submittedAt >= :startOfDay")
    long countUserApplicationsToday(@Param("userId") Long userId,
                                    @Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Find pending applications older than specified days (for auto-review)
     */
    @Query("SELECT a FROM Application a WHERE a.status = 'PENDING' " +
            "AND a.submittedAt < :cutoffDate")
    List<Application> findPendingApplicationsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get application statistics for an opportunity
     */
    @Query("SELECT a.status, COUNT(a) FROM Application a " +
            "WHERE a.opportunityId = :opportunityId GROUP BY a.status")
    List<Object[]> getApplicationStatisticsByOpportunity(@Param("opportunityId") Long opportunityId);
}