package com.youthconnect.ai.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Fallback implementation for {@link UserServiceClient}.
 * <p>
 * Provides default responses when the User Service is unavailable.
 * This ensures that dependent services can still function with
 * degraded but predictable behavior.
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ResponseEntity<Map<String, Object>> getUserProfile(Long userId) {
        log.warn("User service unavailable - using fallback for user profile: {}", userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "role", "UNKNOWN",
                "interests", List.of(),
                "location", "UNAVAILABLE",
                "fallback", true
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getUserInterests(Long userId) {
        log.warn("User service unavailable - using fallback for user interests: {}", userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "interests", List.of(),
                "fallback", true
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getUserActivityData(Long userId) {
        log.warn("User service unavailable - using fallback for user activity data: {}", userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "sessionCount", 0,
                "averageSessionDuration", 0.0,
                "fallback", true
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getUsersByRole(String role) {
        log.warn("User service unavailable - using fallback for users by role: {}", role);
        return ResponseEntity.ok(Map.of(
                "role", role,
                "users", List.of(),
                "fallback", true
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> checkHealth() {
        log.warn("User service health check failed - using fallback");
        return ResponseEntity.ok(Map.of(
                "status", "DOWN",
                "fallback", true
        ));
    }
}
