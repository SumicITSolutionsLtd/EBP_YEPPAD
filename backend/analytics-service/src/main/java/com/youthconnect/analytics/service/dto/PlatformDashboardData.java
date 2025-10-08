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
public class PlatformDashboardData {
    private int periodDays;
    private int totalUsers;
    private int newUsers;
    private int activeUsers;
    private int totalOpportunities;
    private int newOpportunities;
    private int totalApplications;
    private UserEngagementData engagement;
    private List<DailyMetric> userGrowthTrend;
    private List<DailyMetric> opportunityTrend;
    private Map<String, Integer> roleDistribution;
    private LocalDateTime generatedAt;
}
