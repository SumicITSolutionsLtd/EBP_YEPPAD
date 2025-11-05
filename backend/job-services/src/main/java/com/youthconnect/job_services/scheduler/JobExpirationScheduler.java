package com.youthconnect.job_services.scheduler;

import com.youthconnect.job_services.entity.Job;
import com.youthconnect.job_services.enums.JobStatus;
import com.youthconnect.job_services.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job Expiration Scheduler - FIXED
 *
 * Automatically expires jobs past their expiration date.
 * Uses corrected repository method names.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobExpirationScheduler {

    private final JobRepository jobRepository;

    /**
     * Expire old jobs
     * Runs daily at 2:00 AM
     * FIXED: Uses correct repository method name
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void expireOldJobs() {
        log.info("Running job expiration scheduler...");

        // FIXED: Use corrected method name
        List<Job> expiredJobs = jobRepository.findByStatusAndExpiresAtBeforeAndIsDeletedFalse(
                JobStatus.PUBLISHED,
                LocalDateTime.now()
        );

        if (expiredJobs.isEmpty()) {
            log.info("No jobs to expire");
            return;
        }

        expiredJobs.forEach(job -> {
            job.setStatus(JobStatus.EXPIRED);
            job.setClosedAt(LocalDateTime.now());
        });

        jobRepository.saveAll(expiredJobs);

        log.info("Expired {} jobs", expiredJobs.size());
    }

    /**
     * Send expiration reminders
     * Runs daily at 9:00 AM
     * Notifies job posters about jobs expiring in 3 days
     * FIXED: Uses correct repository method name
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendExpirationReminders() {
        log.info("Sending job expiration reminders...");

        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        LocalDateTime fourDaysFromNow = LocalDateTime.now().plusDays(4);

        // FIXED: Use corrected method name
        List<Job> expiringJobs = jobRepository.findByStatusAndExpiresAtBetweenAndIsDeletedFalse(
                JobStatus.PUBLISHED,
                threeDaysFromNow,
                fourDaysFromNow
        );

        // TODO: Send notifications to job posters
        log.info("Found {} jobs expiring in 3 days", expiringJobs.size());
    }
}