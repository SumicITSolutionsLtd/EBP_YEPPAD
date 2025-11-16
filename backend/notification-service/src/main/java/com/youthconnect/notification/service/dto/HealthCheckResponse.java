package com.youthconnect.notification.service.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * HEALTH CHECK RESPONSE DTO
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Standardized health check response.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse {

    private String status; // UP, DEGRADED, DOWN
    private String service;
    private LocalDateTime timestamp;
    private Map<String, ServiceHealth> checks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHealth {
        private boolean healthy;
        private String status;
        private String provider;
        private Long responseTime;
        private String error;
    }
}
