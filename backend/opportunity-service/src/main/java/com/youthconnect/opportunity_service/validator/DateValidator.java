package com.youthconnect.opportunity_service.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Utility validator for date-related validations
 */
@Component
@Slf4j
public class DateValidator {

    /**
     * Check if a date is in the future
     */
    public boolean isFutureDate(LocalDateTime date) {
        return date != null && date.isAfter(LocalDateTime.now());
    }

    /**
     * Check if a date is in the past
     */
    public boolean isPastDate(LocalDateTime date) {
        return date != null && date.isBefore(LocalDateTime.now());
    }

    /**
     * Get days until a specific date
     */
    public long getDaysUntil(LocalDateTime targetDate) {
        if (targetDate == null) {
            return -1;
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), targetDate);
    }

    /**
     * Check if deadline is approaching (within specified days)
     */
    public boolean isDeadlineApproaching(LocalDateTime deadline, int thresholdDays) {
        if (deadline == null) {
            return false;
        }
        long daysUntil = getDaysUntil(deadline);
        return daysUntil >= 0 && daysUntil <= thresholdDays;
    }

    /**
     * Check if deadline has passed
     */
    public boolean hasDeadlinePassed(LocalDateTime deadline) {
        return isPastDate(deadline);
    }
}