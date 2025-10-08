package com.youthconnect.auth_service.dto.response;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long userId;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private String role;
    private boolean active;
}