package com.youthconnect.edge_functions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Service Health DTO
 * Indicates health status of dependent services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthDTO {
    private String serviceName;
    private String status;  // UP, DOWN, DEGRADED
    private Long responseTimeMs;
    private String message;
    private LocalDateTime lastChecked;
}
