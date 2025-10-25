package com.youthconnect.edge_functions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    // Core identification
    private Long userId;  // FIXED: Was missing
    private String email;
    private String phoneNumber;

    // Profile information
    private String firstName;
    private String lastName;

    // Role and status
    private String userType;  // Maps to role enum
    private Boolean verified;
    private Boolean active;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}