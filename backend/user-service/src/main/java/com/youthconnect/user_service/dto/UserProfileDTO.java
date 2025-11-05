package com.youthconnect.user_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) for sending user profile information to other services or clients.
 * This ensures that sensitive information (like password hashes) is never exposed.
 */
@Data
@Builder // Provides a convenient way to build this object
public class UserProfileDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String profession;
    private String district;
    private String description;
    private LocalDate dateOfBirth;
}