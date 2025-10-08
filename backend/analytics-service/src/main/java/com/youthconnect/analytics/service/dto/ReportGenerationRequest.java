package com.youthconnect.analytics.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerationRequest {
    private String reportType; // NGO_DASHBOARD, FUNDER_REPORT, PLATFORM_ANALYTICS
    private String format; // PDF, CSV, XLSX
    private LocalDate startDate;
    private LocalDate endDate;
    private Long organizationId; // NGO or Funder ID
    private Map<String, Object> parameters;
}
