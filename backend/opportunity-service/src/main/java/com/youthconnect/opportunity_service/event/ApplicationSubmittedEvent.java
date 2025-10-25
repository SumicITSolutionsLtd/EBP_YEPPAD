package com.youthconnect.opportunity_service.event;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Event published when an application is submitted
 * Triggers notifications and analytics tracking
 */
@Data
@Builder
public class ApplicationSubmittedEvent {
    private Long applicationId;
    private Long opportunityId;
    private Long applicantId;
    private LocalDateTime timestamp;
}