package com.youthconnect.content_service.controller;

import com.youthconnect.content_service.dto.request.CreateCommentRequest;
import com.youthconnect.content_service.dto.response.ApiResponse;
import com.youthconnect.content_service.dto.response.CommentDTO;
import com.youthconnect.content_service.entity.Post;
import com.youthconnect.content_service.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Comments
 *
 * <p>Handles all comment-related operations including:</p>
 * <ul>
 *   <li>Comment creation with threading support</li>
 *   <li>Comment retrieval with nested replies</li>
 *   <li>Voting (upvote/downvote)</li>
 *   <li>Solution marking for forum questions</li>
 *   <li>Soft deletion</li>
 * </ul>
 *
 * <p><strong>Authentication:</strong></p>
 * <p>All endpoints except GET require authentication.
 * In production, user ID will be extracted from JWT token.
 * For development, X-User-Id header is used.</p>
 *
 * @author Douglas Kings Kato
 * @version 1.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/api/content/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comments", description = "Comment management on posts")
public class CommentController {

    private final ContentService contentService;

    // ========================================
    // COMMENT RETRIEVAL
    // ========================================

    /**
     * Get all comments for a post.
     *
     * <p>Returns threaded comments with nested replies up to 5 levels deep.
     * Comments are sorted by:</p>
     * <ul>
     *   <li>Solution comments first (if marked)</li>
     *   <li>Net vote score (upvotes - downvotes)</li>
     *   <li>Creation date (oldest first at same vote level)</li>
     * </ul>
     *
     * @param postId Post ID to fetch comments for
     * @return List of top-level comments with nested replies
     *
     * @apiNote Example Request:
     * <pre>
     * GET /api/content/comments?postId=123
     * </pre>
     *
     * @apiNote Example Response:
     * <pre>
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "commentId": 1,
     *       "postId": 123,
     *       "commentText": "Great question! Here's how...",
     *       "upvotesCount": 15,
     *       "downvotesCount": 2,
     *       "isSolution": true,
     *       "replies": [...]
     *     }
     *   ]
     * }
     * </pre>
     */
    @GetMapping
    @Operation(
            summary = "Get comments for a post",
            description = "Retrieves all approved comments for a post with threaded replies"
    )
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getCommentsByPostId(
            @Parameter(description = "Post ID", required = true, example = "123")
            @RequestParam Long postId) {

        log.debug("GET /api/content/comments?postId={}", postId);

        List<CommentDTO> comments = contentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    // ========================================
    // COMMENT CREATION
    // ========================================

    /**
     * Create a new comment on a post.
     *
     * <p>Supports both top-level comments and threaded replies.
     * Maximum thread depth is 5 levels.</p>
     *
     * <p><strong>Validation:</strong></p>
     * <ul>
     *   <li>Comment text: 5-2000 characters</li>
     *   <li>Post must exist and be active</li>
     *   <li>Parent comment must exist (if replying)</li>
     *   <li>Thread depth must be <= 5</li>
     * </ul>
     *
     * <p><strong>Auto-Moderation:</strong></p>
     * <p>Comments containing prohibited content are flagged for manual review.</p>
     *
     * @param request Comment creation data
     * @param authorId User ID (from JWT in production)
     * @return Created comment DTO
     *
     * @apiNote Example Request:
     * <pre>
     * POST /api/content/comments
     * X-User-Id: 1
     * Content-Type: application/json
     *
     * {
     *   "postId": 123,
     *   "commentText": "This is my comment text...",
     *   "parentCommentId": null
     * }
     * </pre>
     */
    @PostMapping
    @Operation(
            summary = "Create comment",
            description = "Adds a new comment or reply to a post"
    )
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            @Parameter(description = "Author user ID (from JWT in production)")
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long authorId) {

        log.info("POST /api/content/comments - Author: {}, Post: {}", authorId, request.getPostId());

        CommentDTO createdComment = contentService.createComment(request, authorId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment created successfully", createdComment));
    }

    // ========================================
    // COMMENT VOTING
    // ========================================

    /**
     * Vote on a comment (upvote or downvote).
     *
     * <p><strong>Voting Behavior:</strong></p>
     * <ul>
     *   <li>First vote: Adds vote and increments count</li>
     *   <li>Same vote again: Removes vote (toggle)</li>
     *   <li>Different vote: Changes vote and updates counts</li>
     * </ul>
     *
     * <p>Users cannot vote on their own comments.</p>
     *
     * @param commentId Comment ID to vote on
     * @param voteType Vote type: UPVOTE or DOWNVOTE
     * @param userId User ID (from JWT in production)
     * @return Success message
     *
     * @apiNote Example Request:
     * <pre>
     * POST /api/content/comments/456/vote?voteType=UPVOTE
     * X-User-Id: 2
     * </pre>
     */
    @PostMapping("/{commentId}/vote")
    @Operation(
            summary = "Vote on comment",
            description = "Upvote or downvote a comment"
    )
    public ResponseEntity<ApiResponse<Void>> voteComment(
            @Parameter(description = "Comment ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "Vote type: UPVOTE or DOWNVOTE", required = true)
            @RequestParam Post.VoteType voteType,
            @Parameter(description = "User ID (from JWT in production)")
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {

        log.debug("POST /api/content/comments/{}/vote - User: {}, Type: {}", commentId, userId, voteType);

        contentService.voteComment(commentId, userId, voteType);
        return ResponseEntity.ok(ApiResponse.success("Vote recorded successfully", null));
    }

    // ========================================
    // SOLUTION MARKING
    // ========================================

    /**
     * Mark a comment as the solution to a forum question.
     *
     * <p><strong>Authorization:</strong></p>
     * <ul>
     *   <li>Only the post author can mark solutions</li>
     *   <li>Only works for FORUM_QUESTION post type</li>
     *   <li>Only one comment can be marked as solution per post</li>
     * </ul>
     *
     * <p>When a comment is marked as solution:</p>
     * <ul>
     *   <li>Previous solution is unmarked (if any)</li>
     *   <li>Solution author receives notification</li>
     *   <li>Solution author gains reputation points</li>
     * </ul>
     *
     * @param commentId Comment ID to mark as solution
     * @param userId Post author user ID
     * @return Success message
     *
     * @apiNote Example Request:
     * <pre>
     * PUT /api/content/comments/789/solution
     * X-User-Id: 1
     * </pre>
     */
    @PutMapping("/{commentId}/solution")
    @Operation(
            summary = "Mark as solution",
            description = "Marks a comment as the solution to a forum question (post author only)"
    )
    public ResponseEntity<ApiResponse<Void>> markAsSolution(
            @Parameter(description = "Comment ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "Post author user ID (from JWT in production)")
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {

        log.info("PUT /api/content/comments/{}/solution - User: {}", commentId, userId);

        contentService.markCommentAsSolution(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success("Comment marked as solution", null));
    }

    // ========================================
    // COMMENT DELETION
    // ========================================

    /**
     * Delete a comment (soft delete).
     *
     * <p><strong>Authorization:</strong></p>
     * <ul>
     *   <li>Comment author can delete their own comments</li>
     *   <li>Moderators (NGO/Admin) can delete any comment</li>
     * </ul>
     *
     * <p><strong>Soft Delete Behavior:</strong></p>
     * <ul>
     *   <li>Comment is hidden from feed (isActive = false)</li>
     *   <li>Data is retained for audit purposes</li>
     *   <li>Post comment count is decremented</li>
     *   <li>Replies are not affected (orphaned)</li>
     * </ul>
     *
     * @param commentId Comment ID to delete
     * @param userId User ID attempting deletion
     * @return Success message
     *
     * @apiNote Example Request:
     * <pre>
     * DELETE /api/content/comments/999
     * X-User-Id: 1
     * </pre>
     */
    @DeleteMapping("/{commentId}")
    @Operation(
            summary = "Delete comment",
            description = "Soft deletes a comment (author or moderator only)"
    )
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @Parameter(description = "Comment ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "User ID (from JWT in production)")
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {

        log.info("DELETE /api/content/comments/{} - User: {}", commentId, userId);

        contentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }

    // ========================================
    // COMMENT REPORTING (OPTIONAL)
    // ========================================

    /**
     * Report a comment for moderation.
     *
     * <p>Users can report comments that violate community guidelines.
     * Reported comments are added to the moderation queue for NGO review.</p>
     *
     * <p><strong>Report Reasons:</strong></p>
     * <ul>
     *   <li>SPAM - Unsolicited advertising or repetitive content</li>
     *   <li>HARASSMENT - Personal attacks or bullying</li>
     *   <li>MISINFORMATION - False or misleading information</li>
     *   <li>INAPPROPRIATE - Offensive or inappropriate content</li>
     *   <li>OTHER - Custom reason provided by reporter</li>
     * </ul>
     *
     * @param commentId Comment ID to report
     * @param reason Report reason
     * @param userId User ID reporting the comment
     * @return Success message
     *
     * @apiNote Example Request:
     * <pre>
     * POST /api/content/comments/888/report
     * X-User-Id: 3
     * Content-Type: application/json
     *
     * {
     *   "reason": "SPAM",
     *   "description": "This comment contains advertising"
     * }
     * </pre>
     */
    @PostMapping("/{commentId}/report")
    @Operation(
            summary = "Report comment",
            description = "Reports a comment for moderator review"
    )
    public ResponseEntity<ApiResponse<Void>> reportComment(
            @Parameter(description = "Comment ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "Report reason")
            @RequestParam String reason,
            @Parameter(description = "User ID (from JWT in production)")
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {

        log.warn("POST /api/content/comments/{}/report - Reason: {} - Reporter: {}", commentId, reason, userId);

        // TODO: Implement report functionality
        // contentService.reportComment(commentId, userId, reason);

        return ResponseEntity.ok(ApiResponse.success("Comment reported for review", null));
    }

    // ========================================
    // BULK OPERATIONS (FOR MODERATION)
    // ========================================

    /**
     * Get user's comments (for profile view).
     *
     * @param userId User ID
     * @param page Page number
     * @param size Page size
     * @return User's comments
     */
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get user's comments",
            description = "Retrieves all comments by a specific user"
    )
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getUserComments(
            @Parameter(description = "User ID")
            @PathVariable Long userId,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /api/content/comments/user/{} - page: {}, size: {}", userId, page, size);

        // TODO: Implement getUserComments method in service
        // Page<CommentDTO> comments = contentService.getUserComments(userId, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
}