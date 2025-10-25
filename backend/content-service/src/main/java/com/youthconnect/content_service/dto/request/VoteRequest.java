package com.youthconnect.content_service.dto.request;

import com.youthconnect.content_service.entity.Post;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for voting on posts/comments
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteRequest {

    @NotNull(message = "Vote type is required")
    private Post.VoteType voteType;

    // Either postId or commentId must be provided
    private Long postId;
    private Long commentId;
}