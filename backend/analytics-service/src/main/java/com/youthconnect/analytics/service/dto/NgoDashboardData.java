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
public class NgoDashboardData {
    private Long ngoId;
    private int periodDays;
    private int totalOpportunities;
    private int totalApplications;
    private int approvedApplications;
    private int activeUsers;
    private double applicationRate;
    private double approvalRate;
    private List<DailyMetric> applicationTrend;
    private List<DailyMetric> userRegistrationTrend;
    private Map<String, Integer> geographicDistribution;
    private List<OpportunityPerformance> topOpportunities;
    private LocalDateTime generatedAt;
}
