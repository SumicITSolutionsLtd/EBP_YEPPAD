package com.youthconnect.content_service.client;

import com.youthconnect.content_service.dto.response.UserBasicInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Feign Client for User Service Integration
 *
 * Retrieves user profile information for content enrichment
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@FeignClient(
        name = "user-service",
        path = "/api/users",
        fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    /**
     * Get user basic information for content display
     *
     * @param userId User ID
     * @return Basic user info (name, profile picture, role)
     */
    @GetMapping("/{userId}/basic")
    UserBasicInfoDto getUserBasicInfo(@PathVariable Long userId);

    /**
     * Get multiple users' basic information in bulk
     *
     * @param userIds List of user IDs
     * @return Map of user ID to basic info
     */
    @GetMapping("/bulk")
    List<UserBasicInfoDto> getUsersBulk(@RequestParam List<Long> userIds);

    /**
     * Get user's preferred language for content delivery
     *
     * @param userId User ID
     * @return Language code (en, lg, lur, lgb)
     */
    @GetMapping("/{userId}/preferred-language")
    String getUserPreferredLanguage(@PathVariable Long userId);
}

/**
 * Fallback when User Service is unavailable
 */
@Component
@Slf4j
class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserBasicInfoDto getUserBasicInfo(Long userId) {
        log.warn("User service unavailable - Returning default user info for userId={}", userId);
        return UserBasicInfoDto.builder()
                .userId(userId)
                .displayName("Unknown User")
                .role("YOUTH")
                .build();
    }

    @Override
    public List<UserBasicInfoDto> getUsersBulk(List<Long> userIds) {
        log.warn("User service unavailable - Returning default user info for {} users", userIds.size());
        return userIds.stream()
                .map(id -> UserBasicInfoDto.builder()
                        .userId(id)
                        .displayName("Unknown User")
                        .role("YOUTH")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public String getUserPreferredLanguage(Long userId) {
        log.warn("User service unavailable - Returning default language for userId={}", userId);
        return "en"; // Default to English
    }
}