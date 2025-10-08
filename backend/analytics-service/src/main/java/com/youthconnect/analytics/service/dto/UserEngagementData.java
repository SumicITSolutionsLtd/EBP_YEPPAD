package com.youthconnect.analytics.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEngagementData {
    private int periodDays;
    private int dailyActiveUsers;
    private int weeklyActiveUsers;
    private int monthlyActiveUsers;
    private double averageSessionDuration;
    private double averagePagesPerSession;
    private double bounceRate;
    private double engagementScore;
    private Map<String, Integer> featureUsage;
    private LocalDateTime calculatedAt;
}
