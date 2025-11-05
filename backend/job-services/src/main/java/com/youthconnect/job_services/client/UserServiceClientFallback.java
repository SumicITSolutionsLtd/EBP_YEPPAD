package com.youthconnect.job_services.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * User Service Client Fallback
 *
 * Provides fallback responses when user-service is unavailable.
 * This prevents cascading failures when user service is down.
 *
 * Circuit Breaker Pattern Implementation:
 * - If user-service fails, return safe default values
 * - Log the failure for monitoring
 * - Allow job service to continue functioning
 * @author Douglas Kings Kato
 * @since 1.0.0
 */
@Slf4j
@Component
public class UserServiceClientFallback implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {

            /**
             * Fallback for userExists()
             * Returns true to allow operations to continue
             * (Better to allow than to block all operations)
             */
            @Override
            public Boolean userExists(UUID userId) {
                log.error("User service unavailable for userExists({}). Cause: {}. " +
                                "Returning true to allow operation.",
                        userId, cause.getMessage());
                // Return true to be permissive when service is down
                // Real validation will happen when user-service recovers
                return true;
            }

            /**
             * Fallback for getUserSummary()
             * Returns minimal user profile
             */
            @Override
            public UserProfileResponse getUserSummary(UUID userId) {
                log.error("User service unavailable for getUserSummary({}). Cause: {}. " +
                                "Returning fallback profile.",
                        userId, cause.getMessage());

                return new UserProfileResponse(
                        userId,
                        "unavailable@system.local",
                        "User #" + userId,
                        "UNKNOWN",
                        "Organization Unavailable",
                        true,
                        false
                );
            }

            /**
             * Fallback for canUserPostJobs()
             * Returns true for existing operations to continue
             */
            @Override
            public Boolean canUserPostJobs(UUID userId) {
                log.error("User service unavailable for canUserPostJobs({}). Cause: {}. " +
                                "Returning true to allow operation.",
                        userId, cause.getMessage());
                return true;
            }

            /**
             * Fallback for getUserOrganization()
             * Returns placeholder organization name
             */
            @Override
            public String getUserOrganization(UUID userId) {
                log.error("User service unavailable for getUserOrganization({}). Cause: {}. " +
                                "Returning fallback organization name.",
                        userId, cause.getMessage());
                return "Organization (ID: " + userId + ")";
            }
        };
    }
}
