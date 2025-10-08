package com.youthconnect.mentor_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionRequest {
    private Long mentorId;
    private Long menteeId; // In a real app, this is derived from the JWT
    private LocalDateTime sessionDatetime;
    private String topic;
}
