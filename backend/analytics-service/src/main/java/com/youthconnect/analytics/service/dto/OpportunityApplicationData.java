package com.youthconnect.analytics.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityApplicationData {
    private Long opportunityId;
    private String opportunityTitle;
    private int applicationsCount;
    private int pendingCount;
    private int approvedCount;
    private int rejectedCount;
    private double successRate;
}
