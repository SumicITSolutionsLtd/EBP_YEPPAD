package com.youthconnect.content_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PostDTO {
    private Long postId;
    private Long authorId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    // In a real app, you'd add author details like name and profile picture.
}