package com.youthconnect.user_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * JOB SERVICE CLIENT FALLBACK - CIRCUIT BREAKER PATTERN
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Provides fallback responses when job-service is unavailable or experiencing
 * issues. This ensures user-service remains functional even if job-service fails.
 *
 * Pattern: Circuit Breaker with Graceful Degradation
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Component
public class JobServiceClientFallback implements JobServiceClient {

    /**
     * Fallback for application summary
     *
     * Returns empty/zero statistics instead of failing the entire user profile request.
     */
    @Override
    public ApplicationSummaryResponse getUserApplicationSummary(UUID userId) {
        log.warn("⚠️ Job service unavailable - returning empty application summary for user: {}", userId);

        return new ApplicationSummaryResponse(
                0,      // totalApplications
                0,      // pendingApplications
                0,      // approvedApplications
                0,      // rejectedApplications
                0,      // withdrawnApplications
                0.0     // successRate
        );
    }

    /**
     * Fallback for active applications count
     */
    @Override
    public Integer getUserActiveApplicationsCount(UUID userId) {
        log.warn("⚠️ Job service unavailable - returning 0 for active applications count: {}", userId);
        return 0;
    }

    /**
     * Fallback for application existence check
     *
     * Returns false (not applied) to avoid blocking user from applying if service is down.
     */
    @Override
    public Boolean hasUserAppliedToJob(UUID userId, Long jobId) {
        log.warn("⚠️ Job service unavailable - assuming user has NOT applied: userId={}, jobId={}",
                userId, jobId);
        return false;
    }

    /**
     * Fallback for recent applications
     */
    @Override
    public java.util.List<RecentApplicationDto> getUserRecentApplications(UUID userId, int limit) {
        log.warn("⚠️ Job service unavailable - returning empty applications list for user: {}", userId);
        return Collections.emptyList();
    }

    /**
     * Fallback for current employment
     */
    @Override
    public CurrentEmploymentDto getCurrentEmployment(UUID userId) {
        log.warn("⚠️ Job service unavailable - returning null for current employment: {}", userId);
        return null;
    }
}