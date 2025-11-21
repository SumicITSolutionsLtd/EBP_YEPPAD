package com.youthconnect.ai.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign client for communication with the User Service.
 * <p>
 * Provides methods to fetch user profiles, interests, activities,
 * and role-based user data for recommendation algorithms.
 * Also includes a health check endpoint.
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    /**
     * Retrieves the profile details of a specific user.
     *
     * @param userId ID of the user
     * @return user profile as a map wrapped in {@link ResponseEntity}
     */
    @GetMapping("/api/v1/users/{userId}/profile")
    ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable Long userId);

    /**
     * Retrieves the interests of a specific user.
     *
     * @param userId ID of the user
     * @return user interests as a map wrapped in {@link ResponseEntity}
     */
    @GetMapping("/api/v1/users/{userId}/interests")
    ResponseEntity<Map<String, Object>> getUserInterests(@PathVariable Long userId);

    /**
     * Retrieves activity data of a specific user (e.g., engagement, actions).
     *
     * @param userId ID of the user
     * @return user activity data as a map wrapped in {@link ResponseEntity}
     */
    @GetMapping("/api/v1/users/{userId}/activity")
    ResponseEntity<Map<String, Object>> getUserActivityData(@PathVariable Long userId);

    /**
     * Retrieves all users with a given role.
     *
     * @param role name of the role (e.g., "admin", "member")
     * @return users matching the role as a map wrapped in {@link ResponseEntity}
     */
    @GetMapping("/api/v1/users/role/{role}")
    ResponseEntity<Map<String, Object>> getUsersByRole(@PathVariable String role);

    /**
     * Performs a health check on the User Service.
     *
     * @return health status as a map wrapped in {@link ResponseEntity}
     */
    @GetMapping("/actuator/health")
    ResponseEntity<Map<String, Object>> checkHealth();
}
