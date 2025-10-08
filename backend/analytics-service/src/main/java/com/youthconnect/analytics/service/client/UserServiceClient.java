package com.youthconnect.analytics.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign client for Analytics service to get user data
 */
@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/stats")
    ResponseEntity<Map<String, Object>> getUserStatistics();

    @GetMapping("/api/v1/users/count")
    ResponseEntity<Map<String, Object>> getUserCount();

    @GetMapping("/api/v1/users/registrations/recent")
    ResponseEntity<Map<String, Object>> getRecentRegistrations(@RequestParam int days);

    @GetMapping("/actuator/health")
    ResponseEntity<Map<String, Object>> checkHealth();
}
