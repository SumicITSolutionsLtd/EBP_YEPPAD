package com.youthconnect.content_service.controller;

import com.youthconnect.content_service.dto.request.CreatePostRequest;
import com.youthconnect.content_service.dto.response.ApiResponse;
import com.youthconnect.content_service.dto.response.PostDTO;
import com.youthconnect.content_service.entity.Post;
import com.youthconnect.content_service.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Community Posts
 * Handles post creation, retrieval, voting, and feed management
 */
@RestController
@RequestMapping("/api/content/posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Community Posts", description = "Community forum post management")
public class PostController {

    private final ContentService contentService;

    /**
     * Get paginated list of posts for community feed
     */
    @GetMapping
    @Operation(summary = "Get all posts", description = "Retrieves paginated list of approved posts")
    public ResponseEntity<ApiResponse<Page<PostDTO>>> getAllPosts(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /api/content/posts - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<PostDTO> posts = contentService.getAllPosts(pageable);

        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * Get single post by ID
     */
    @GetMapping("/{postId}")
    @Operation(summary = "Get post by ID", description = "Retrieves a single post with full details")
    public ResponseEntity<ApiResponse<PostDTO>> getPostById(
            @Parameter(description = "Post ID")
            @PathVariable Long postId) {

        log.debug("GET /api/content/posts/{}", postId);

        PostDTO post = contentService.getPostById(postId);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    /**
     * Create new post
     */
    @PostMapping
    @Operation(summary = "Create post", description = "Creates a new community post")
    public ResponseEntity<ApiResponse<PostDTO>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @Parameter(description = "Author user ID (from JWT in production)")
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long authorId) {

        log.info("POST /api/content/posts - Author: {}", authorId);

        PostDTO createdPost = contentService.createPost(request, authorId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", createdPost));
    }

    /**
     * Vote on a post
     */
    @PostMapping("/{postId}/vote")
    @Operation(summary = "Vote on post", description = "Upvote or downvote a post")
    public ResponseEntity<ApiResponse<String>> votePost(
            @Parameter(description = "Post ID")
            @PathVariable Long postId,
            @Parameter(description = "Vote type: UPVOTE or DOWNVOTE")
            @RequestParam Post.VoteType voteType,
            @Parameter(description = "User ID (from JWT in production)")
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {

        log.debug("POST /api/content/posts/{}/vote - User: {}, Type: {}", postId, userId, voteType);

        contentService.votePost(postId, userId, voteType);
        return ResponseEntity.ok(ApiResponse.success("Vote recorded successfully", null));
    }
}