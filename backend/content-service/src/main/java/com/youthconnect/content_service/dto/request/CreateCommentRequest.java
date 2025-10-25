package com.youthconnect.content_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a comment on a post
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentRequest {

    @NotNull(message = "Post ID is required")
    private Long postId;

    @NotBlank(message = "Comment text is required")
    @Size(min = 5, max = 2000, message = "Comment must be between 5 and 2000 characters")
    private String commentText;

    // Optional: For threaded replies
    private Long parentCommentId;
}