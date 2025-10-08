package com.youthconnect.edge_functions.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MentorshipSessionDTO {
    private Long id;
    private Long mentorId;
    private Long menteeId;
    private LocalDateTime sessionDateTime;
    private String topic;
    private String status;
    private String question;
    private Boolean isPublic;
    private String category;
}