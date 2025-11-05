package com.youthconnect.job_services.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 *
 * Provides public health check endpoints for monitoring and load balancing.
 * Does NOT require authentication - must be accessible to infrastructure.
 *
 * Endpoints:
 * - /health - Simple UP/DOWN status
 * - /health/detailed - Comprehensive health information
 *
 * @author Douglas Kings Kato
 * @since 1.0.0
 */
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "Service health monitoring endpoints (public)")
public class HealthCheckController {

    private final DataSource dataSource;

    /**
     * Simple health check endpoint
     *
     * Returns basic UP/DOWN status for load balancers and monitoring tools.
     * This endpoint is PUBLIC and does not require authentication.
     *
     * @return ResponseEntity with health status
     */
    @GetMapping
    @Operation(
            summary = "Basic health check",
            description = "Returns simple UP status if service is running. Used by load balancers and monitoring."
    )
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "job-services");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health check endpoint
     *
     * Provides comprehensive health information including:
     * - Service status
     * - Database connectivity
     * - Version information
     * - Uptime
     *
     * This endpoint is PUBLIC for infrastructure monitoring.
     *
     * @return ResponseEntity with detailed health information
     */
    @GetMapping("/detailed")
    @Operation(
            summary = "Detailed health check",
            description = "Returns comprehensive health information including database status"
    )
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> components = new HashMap<>();

        // Service status
        response.put("status", "UP");
        response.put("service", "job-services");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());

        // Database health check
        boolean dbHealthy = checkDatabaseConnection();
        components.put("database", Map.of(
                "status", dbHealthy ? "UP" : "DOWN",
                "type", "PostgreSQL"
        ));

        // Overall health
        boolean allHealthy = dbHealthy;
        response.put("status", allHealthy ? "UP" : "DEGRADED");
        response.put("components", components);

        return ResponseEntity.ok(response);
    }

    /**
     * Check database connectivity
     *
     * Attempts to get a database connection to verify connectivity.
     *
     * @return true if database is accessible, false otherwise
     */
    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2); // 2 second timeout
        } catch (Exception e) {
            return false;
        }
    }
}