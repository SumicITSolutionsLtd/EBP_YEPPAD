package com.youthconnect.opportunity_service.dto;

import com.youthconnect.opportunity_service.entity.Opportunity;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for representing an Opportunity in API responses.
 */
@Data
@Builder
public class OpportunityDTO {
    private Long opportunityId;
    private Long postedById;
    private Opportunity.OpportunityType opportunityType;
    private String title;
    private String description;
    private Opportunity.Status status;
    private LocalDateTime applicationDeadline;
    private LocalDateTime createdAt;
}
