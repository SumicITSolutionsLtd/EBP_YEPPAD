package com.youthconnect.edge_functions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Mentorship Session DTO
 * Handles session scheduling and management
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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