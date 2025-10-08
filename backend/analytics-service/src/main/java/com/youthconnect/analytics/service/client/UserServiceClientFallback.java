package com.youthconnect.analytics.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fallback for Analytics service User Service Client
 */
@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        log.warn("User service unavailable - using fallback statistics");
        return ResponseEntity.ok(Map.of(
                "totalUsers", 2500,
                "activeUsers", 400,
                "newUsers", 150,
                "fallback", true
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getUserCount() {
        log.warn("User service unavailable - using fallback user count");
        return ResponseEntity.ok(Map.of(
                "count", 2500,
                "fallback", true
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getRecentRegistrations(int days) {
        log.warn("User service unavailable - using fallback registrations");
        return ResponseEntity.ok(Map.of(
                "registrations", 150,
                "days", days,
                "fallback", true
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of("status", "DOWN", "fallback", true));
    }
}
