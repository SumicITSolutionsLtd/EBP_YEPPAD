package com.youthconnect.content_service.dto.request;

import com.youthconnect.content_service.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new community post
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest {

    @NotNull(message = "Post type is required")
    private Post.PostType postType;

    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 255, message = "Title must be between 10 and 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 20, max = 5000, message = "Content must be between 20 and 5000 characters")
    private String content;

    // Optional: For audio questions (USSD users)
    private String audioUrl;
}