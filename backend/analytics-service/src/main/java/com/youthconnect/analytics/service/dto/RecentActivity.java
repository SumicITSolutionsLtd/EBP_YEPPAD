package com.youthconnect.analytics.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivity {
    private String activityType;
    private String description;
    private LocalDateTime timestamp;
    private Long userId;
    private String userRole;
}
