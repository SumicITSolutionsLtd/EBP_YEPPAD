package com.youthconnect.api_gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Health Check Controller
 *
 * Provides health check endpoints for:
 * - Load balancers (AWS ALB, Nginx)
 * - Monitoring systems (Prometheus, Nagios)
 * - Kubernetes liveness/readiness probes
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/controller/
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * Simple health check endpoint
     * Returns 200 OK if gateway is running
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "api-gateway");

        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health check with service registry status
     * Shows all registered microservices
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "api-gateway");

        // Get all registered services from Eureka
        List<String> services = discoveryClient.getServices();
        response.put("discoveredServices", services);
        response.put("serviceCount", services.size());

        // Get detailed info for each service
        Map<String, Object> serviceDetails = new HashMap<>();
        for (String service : services) {
            List<String> instances = discoveryClient.getInstances(service)
                    .stream()
                    .map(instance -> instance.getUri().toString())
                    .collect(Collectors.toList());
            serviceDetails.put(service, instances);
        }
        response.put("serviceInstances", serviceDetails);

        return ResponseEntity.ok(response);
    }

    /**
     * Readiness probe endpoint
     * Used by Kubernetes to determine if gateway can receive traffic
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> response = new HashMap<>();

        // Check if at least one backend service is available
        List<String> services = discoveryClient.getServices();
        boolean hasServices = services.size() > 1; // More than just itself

        if (hasServices) {
            response.put("status", "READY");
            response.put("message", "Gateway is ready to receive traffic");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "NOT_READY");
            response.put("message", "Waiting for backend services to register");
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Liveness probe endpoint
     * Used by Kubernetes to determine if gateway should be restarted
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> live() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ALIVE");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }
}