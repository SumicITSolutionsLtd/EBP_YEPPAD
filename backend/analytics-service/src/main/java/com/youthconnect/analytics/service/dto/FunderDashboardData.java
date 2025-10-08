package com.youthconnect.analytics.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunderDashboardData {
    private Long funderId;
    private int periodDays;
    private double totalFundingCommitted;
    private double totalFundingDisbursed;
    private int fundedProjects;
    private int totalApplications;
    private double averageFundingAmount;
    private double disbursementRate;
    private Map<String, Integer> sectorDistribution;
    private List<ProjectImpact> impactMetrics;
    private List<DailyMetric> fundingTrend;
    private LocalDateTime generatedAt;
}
