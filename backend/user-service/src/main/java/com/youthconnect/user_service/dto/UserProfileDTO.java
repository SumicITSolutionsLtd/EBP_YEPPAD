package com.youthconnect.user_service.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for sending user profile information to other services or clients.
 * This ensures that sensitive information (like password hashes) is never exposed.
 */
@Data
@Builder // Provides a convenient way to build this object
public class UserProfileDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
}