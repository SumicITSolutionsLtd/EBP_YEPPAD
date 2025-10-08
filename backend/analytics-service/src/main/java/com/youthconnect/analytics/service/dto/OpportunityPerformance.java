package com.youthconnect.analytics.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityPerformance {
    private Long opportunityId;
    private String title;
    private int applicationsCount;
    private int approvedCount;
    private double successRate;
    private double avgProcessingDays;
}
