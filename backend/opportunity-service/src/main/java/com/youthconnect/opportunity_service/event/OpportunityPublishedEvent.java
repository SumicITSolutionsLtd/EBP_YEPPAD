package com.youthconnect.opportunity_service.event;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Event published when a new opportunity is created
 * Used for analytics tracking and notifications
 */
@Data
@Builder
public class OpportunityPublishedEvent {
    private Long opportunityId;
    private Long postedById;
    private String opportunityType;
    private String title;
    private LocalDateTime applicationDeadline;
    private LocalDateTime timestamp;
}