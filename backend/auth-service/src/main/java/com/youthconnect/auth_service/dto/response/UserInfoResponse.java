package com.youthconnect.auth_service.dto.response;

import lombok.Data;

import java.util.UUID;

/**
 * User Info Response DTO
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
public class UserInfoResponse {
    private UUID userId;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private String role;
    private boolean active;
}