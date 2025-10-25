package com.youthconnect.opportunity_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

/**
 * Feign client for communicating with User Service
 * Retrieves user profile information and validates user permissions
 */
@FeignClient(name = "user-service", path = "/api/users")
public interface UserServiceClient {

    /**
     * Get user profile by ID
     * Returns basic user information (name, email, role)
     */
    @GetMapping("/{userId}")
    Map<String, Object> getUserProfile(@PathVariable("userId") Long userId);

    /**
     * Verify if user has permission to post opportunities
     * Only NGO, FUNDER, and SERVICE_PROVIDER roles can post
     */
    @GetMapping("/{userId}/can-post-opportunities")
    Boolean canPostOpportunities(@PathVariable("userId") Long userId);

    /**
     * Get user role
     */
    @GetMapping("/{userId}/role")
    String getUserRole(@PathVariable("userId") Long userId);

    /**
     * Check if user is active
     */
    @GetMapping("/{userId}/is-active")
    Boolean isUserActive(@PathVariable("userId") Long userId);

    /**
     * Get user's district (for location-based matching)
     */
    @GetMapping("/{userId}/district")
    String getUserDistrict(@PathVariable("userId") Long userId);
}