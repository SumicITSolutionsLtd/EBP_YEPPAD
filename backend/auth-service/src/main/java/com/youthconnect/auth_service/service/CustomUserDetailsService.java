package com.youthconnect.auth_service.service;

import com.youthconnect.auth_service.client.UserServiceClient;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService Implementation
 *
 * Integrates Spring Security with User Service via Feign client.
 * Loads user details from user-service for authentication.
 *
 * @author DOUGLAS KINGS KATO
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    /**
     * Load User by Username (Email or Phone)
     *
     * Called by Spring Security during authentication.
     * Retrieves user from user-service and converts to UserDetails.
     *
     * @param username User identifier (email or phone)
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for: {}", maskIdentifier(username));

        try {
            // Call user-service to get user information
            ApiResponse<UserInfoResponse> response = userServiceClient.getUserByIdentifier(username);

            // Check if response is valid
            if (response == null || !response.isSuccess() || response.getData() == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }

            UserInfoResponse user = response.getData();

            // Convert to Spring Security UserDetails
            return User.builder()
                    .username(user.getEmail())
                    .password(user.getPasswordHash())
                    .authorities(Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole())
                    ))
                    .accountExpired(false)
                    .accountLocked(!user.isActive())
                    .credentialsExpired(false)
                    .disabled(!user.isActive())
                    .build();

        } catch (Exception e) {
            log.error("Error loading user details: {}", e.getMessage());
            throw new UsernameNotFoundException("User not found: " + username, e);
        }
    }

    /**
     * Mask Identifier for Logging (Privacy)
     */
    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 6) {
            return "***";
        }
        return identifier.substring(0, 3) + "***" + identifier.substring(identifier.length() - 3);
    }
}