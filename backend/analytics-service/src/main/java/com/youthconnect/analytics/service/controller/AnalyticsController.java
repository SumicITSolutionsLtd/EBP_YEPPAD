package com.youthconnect.analytics.service.controller;

import com.youthconnect.analytics.service.dto.*;
import com.youthconnect.analytics_service.dto.*;
import com.youthconnect.analytics.service.service.AnalyticsService;
import com.youthconnect.analytics.service.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ReportGenerationService reportService;

    /**
     * Get NGO dashboard analytics
     */
    @GetMapping("/dashboard/ngo/{ngoId}")
    public ResponseEntity<Map<String, Object>> getNgoDashboard(
            @PathVariable Long ngoId,
            @RequestParam(defaultValue = "30") int days) {

        log.info("Getting NGO dashboard analytics for NGO: {} (last {} days)", ngoId, days);

        try {
            NgoDashboardData dashboardData = analyticsService.getNgoDashboardData(ngoId, days);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "ngoId", ngoId,
                    "period", days + " days",
                    "data", dashboardData
            ));

        } catch (Exception e) {
            log.error("Error generating NGO dashboard for {}: {}", ngoId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to generate dashboard",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get Funder dashboard analytics
     */
    @GetMapping("/dashboard/funder/{funderId}")
    public ResponseEntity<Map<String, Object>> getFunderDashboard(
            @PathVariable Long funderId,
            @RequestParam(defaultValue = "90") int days) {

        log.info("Getting Funder dashboard analytics for Funder: {} (last {} days)", funderId, days);

        try {
            FunderDashboardData dashboardData = analyticsService.getFunderDashboardData(funderId, days);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "funderId", funderId,
                    "period", days + " days",
                    "data", dashboardData
            ));

        } catch (Exception e) {
            log.error("Error generating Funder dashboard for {}: {}", funderId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to generate dashboard",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get platform-wide analytics (for admins)
     */
    @GetMapping("/dashboard/platform")
    public ResponseEntity<Map<String, Object>> getPlatformDashboard(
            @RequestParam(defaultValue = "30") int days) {

        log.info("Getting platform dashboard analytics (last {} days)", days);

        try {
            PlatformDashboardData dashboardData = analyticsService.getPlatformDashboardData(days);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "period", days + " days",
                    "data", dashboardData
            ));

        } catch (Exception e) {
            log.error("Error generating platform dashboard: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to generate dashboard",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get user engagement metrics
     */
    @GetMapping("/engagement/overview")
    public ResponseEntity<Map<String, Object>> getUserEngagementOverview(
            @RequestParam(defaultValue = "7") int days) {

        log.info("Getting user engagement overview (last {} days)", days);

        try {
            UserEngagementData engagementData = analyticsService.getUserEngagementData(days);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "period", days + " days",
                    "engagement", engagementData
            ));

        } catch (Exception e) {
            log.error("Error getting engagement overview: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to get engagement data"
            ));
        }
    }

    /**
     * Get opportunity application analytics
     */
    @GetMapping("/opportunities/applications")
    public ResponseEntity<Map<String, Object>> getOpportunityApplications(
            @RequestParam(required = false) Long ngoId,
            @RequestParam(defaultValue = "30") int days) {

        log.info("Getting opportunity application analytics - NGO: {}, Days: {}", ngoId, days);

        try {
            List<OpportunityApplicationData> applicationData =
                    analyticsService.getOpportunityApplicationData(ngoId, days);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "ngoId", ngoId,
                    "period", days + " days",
                    "applications", applicationData,
                    "count", applicationData.size()
            ));

        } catch (Exception e) {
            log.error("Error getting opportunity applications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to get application data"
            ));
        }
    }

    /**
     * Generate and download report
     */
    @PostMapping("/reports/generate")
    public ResponseEntity<byte[]> generateReport(@RequestBody ReportGenerationRequest request) {
        log.info("Generating report - Type: {}, Format: {}", request.getReportType(), request.getFormat());

        try {
            ReportResult report = reportService.generateReport(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(request.getFormat()));
            headers.setContentDispositionFormData("attachment", report.getFileName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(report.getContent());

        } catch (Exception e) {
            log.error("Error generating report: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get impact metrics for a specific program/opportunity
     */
    @GetMapping("/impact/opportunity/{opportunityId}")
    public ResponseEntity<Map<String, Object>> getOpportunityImpact(@PathVariable Long opportunityId) {
        log.info("Getting impact metrics for opportunity: {}", opportunityId);

        try {
            ImpactMetrics metrics = analyticsService.getOpportunityImpactMetrics(opportunityId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "opportunityId", opportunityId,
                    "impact", metrics
            ));

        } catch (Exception e) {
            log.error("Error getting impact metrics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to get impact metrics"
            ));
        }
    }

    /**
     * Get real-time analytics data
     */
    @GetMapping("/realtime/overview")
    public ResponseEntity<Map<String, Object>> getRealtimeAnalytics() {
        log.info("Getting real-time analytics overview");

        try {
            RealtimeAnalyticsData data = analyticsService.getRealtimeAnalytics();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "timestamp", System.currentTimeMillis(),
                    "realtime", data
            ));

        } catch (Exception e) {
            log.error("Error getting real-time analytics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to get real-time data"
            ));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        boolean dataServiceHealthy = analyticsService.checkDataServiceHealth();
        boolean reportServiceHealthy = reportService.checkServiceHealth();

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "analytics-service",
                "checks", Map.of(
                        "dataService", dataServiceHealthy ? "UP" : "DOWN",
                        "reportService", reportServiceHealthy ? "UP" : "DOWN"
                )
        ));
    }

    private MediaType getMediaType(String format) {
        return switch (format.toUpperCase()) {
            case "PDF" -> MediaType.APPLICATION_PDF;
            case "CSV" -> MediaType.parseMediaType("text/csv");
            case "XLSX" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
