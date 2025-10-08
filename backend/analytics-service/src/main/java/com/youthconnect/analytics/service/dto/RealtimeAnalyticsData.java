package com.youthconnect.analytics.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeAnalyticsData {
    private int currentActiveUsers;
    private int todayRegistrations;
    private int todayApplications;
    private int ongoingSessions;
    private List<RecentActivity> recentActivities;
    private LocalDateTime timestamp;
}
