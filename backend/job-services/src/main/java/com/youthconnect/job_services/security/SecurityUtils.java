package com.youthconnect.job_services.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Security Utilities
 *
 * Helper methods to extract user information from SecurityContext.
 * Provides convenient access to authenticated user details.
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Component
public class SecurityUtils {

    /**
     * Get current authenticated user's UUID
     *
     * @return User UUID or null if not authenticated
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationFilter.JwtUserDetails) {
            return ((JwtAuthenticationFilter.JwtUserDetails) authentication.getDetails()).getUserId();
        }

        return null;
    }

    /**
     * Get current authenticated user's email
     *
     * @return User email or null if not authenticated
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            return authentication.getName();
        }

        return null;
    }

    /**
     * Get current authenticated user's role
     *
     * @return User role or null if not authenticated
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getDetails() instanceof JwtAuthenticationFilter.JwtUserDetails) {
            return ((JwtAuthenticationFilter.JwtUserDetails) authentication.getDetails()).getRole();
        }

        return null;
    }

    /**
     * Check if current user has a specific role
     *
     * @param role Role to check
     * @return true if user has the role
     */
    public static boolean hasRole(String role) {
        String currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equals(role);
    }

    /**
     * Check if user is authenticated
     *
     * @return true if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
