package com.youthconnect.ai.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Job Service Client Fallback for AI Recommendation Service
 *
 * Provides fallback responses when job-service is unavailable.
 * Returns empty or default data to prevent recommendation failures.
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Component
public class JobServiceClientFallback implements FallbackFactory<JobServiceClient> {

    @Override
    public JobServiceClient create(Throwable cause) {
        return new JobServiceClient() {

            @Override
            public JobServiceClient.JobResponse getJobById(Long jobId) {
                log.warn("Job service unavailable for getJobById({}). Cause: {}", jobId, cause.getMessage());
                return null; // AI will skip this job in recommendations
            }

            @Override
            public List<JobServiceClient.JobResponse> getRecentJobs(int limit) {
                log.warn("Job service unavailable for getRecentJobs(). Cause: {}", cause.getMessage());
                return Collections.emptyList(); // Return empty list - no jobs to recommend
            }

            @Override
            public JobServiceClient.JobSearchResponse searchJobs(String keyword, Long categoryId,
                                                                 String workMode, int page, int size) {
                log.warn("Job service unavailable for searchJobs(). Cause: {}", cause.getMessage());
                return new JobServiceClient.JobSearchResponse(
                        Collections.emptyList(), 0, 0, 0
                );
            }

            @Override
            public List<JobServiceClient.JobResponse> getJobsByCategory(Long categoryId) {
                log.warn("Job service unavailable for getJobsByCategory({}). Cause: {}",
                        categoryId, cause.getMessage());
                return Collections.emptyList();
            }

            @Override
            public JobServiceClient.JobStatsResponse getJobStats(Long jobId) {
                log.warn("Job service unavailable for getJobStats({}). Cause: {}", jobId, cause.getMessage());
                return new JobServiceClient.JobStatsResponse(jobId, 0, 0, 0.0, 0);
            }
        };
    }
}