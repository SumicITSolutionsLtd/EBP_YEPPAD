package com.youthconnect.analytics.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectImpact {
    private Long projectId;
    private String projectName;
    private double fundingAmount;
    private int jobsCreated;
    private double revenueGenerated;
    private String status;
    private String impactDescription;
}
