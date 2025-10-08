package com.youthconnect.analytics.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpactMetrics {
    private Long opportunityId;
    private String opportunityTitle;
    private int beneficiariesReached;
    private double totalFundingDisbursed;
    private int jobsCreated;
    private int businessesStarted;
    private double averageIncomeIncrease;
    private double successRate;
    private String impactDescription;
}
