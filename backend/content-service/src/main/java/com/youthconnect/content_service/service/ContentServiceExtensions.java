package com.youthconnect.content_service.service;

import com.youthconnect.content_service.dto.request.*;
import com.youthconnect.content_service.dto.response.*;
import com.youthconnect.content_service.entity.*;
import com.youthconnect.content_service.exception.*;
import com.youthconnect.content_service.mapper.*;
import com.youthconnect.content_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UNIFIED Content Service - Missing Methods Implementation
 *
 * <p>This class adds the missing critical methods for comment deletion,
 * voting on comments, marking solutions, and additional business logic.</p>
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ContentServiceExtensions {

    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;
    private final LearningModuleRepository learningModuleRepository;
    private final ModuleProgressRepository moduleProgressRepository;

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final LearningModuleMapper learningModuleMapper;

    // ========================================
    // COMMENT DELETION
    // ========================================

    /**
     * Deletes a comment (soft delete).
     * Only comment author or moderators can delete.
     *
     * @param commentId Comment ID to delete
     * @param userId User attempting deletion
     * @throws ResourceNotFoundException if comment not found
     * @throws UnauthorizedException if user not authorized
     */
    @Transactional
    @CacheEvict(value = {"comments", "posts"}, allEntries = true)
    public void deleteComment(Long commentId, Long userId) {
        log.info("Deleting comment {} by user {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

        // Authorization check: Only author or moderators can delete
        if (!comment.getAuthorId().equals(userId) && !isUserModerator(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this comment");
        }

        // Soft delete
        comment.setIsActive(false);
        commentRepository.save(comment);

        // Decrement post comment count
        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
        postRepository.save(post);

        log.info("Successfully deleted comment {}", commentId);
    }

    // ========================================
    // COMMENT VOTING
    // ========================================

    /**
     * Handles upvote/downvote on a comment.
     * Toggle behavior: Clicking same vote removes it.
     *
     * @param commentId Comment ID
     * @param userId User ID
     * @param voteType UPVOTE or DOWNVOTE
     */
    @Transactional
    @CacheEvict(value = {"comments", "commentVotes"}, allEntries = true)
    public void voteComment(Long commentId, Long userId, Post.VoteType voteType) {
        log.debug("User {} voting {} on comment {}", userId, voteType, commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Check for existing vote
        CommentVote existingVote = commentVoteRepository
                .findByCommentIdAndUserId(commentId, userId);

        if (existingVote != null) {
            if (existingVote.getVoteType() == voteType) {
                // Remove vote (toggle off)
                commentVoteRepository.delete(existingVote);
                decrementCommentVoteCount(comment, voteType);
                log.debug("Removed vote from comment {}", commentId);
            } else {
                // Change vote
                Post.VoteType oldType = existingVote.getVoteType();
                existingVote.setVoteType(voteType);
                commentVoteRepository.save(existingVote);

                // Update counts
                decrementCommentVoteCount(comment, oldType);
                incrementCommentVoteCount(comment, voteType);
                log.debug("Changed vote on comment {}", commentId);
            }
        } else {
            // New vote
            CommentVote newVote = CommentVote.builder()
                    .commentId(commentId)
                    .userId(userId)
                    .voteType(voteType)
                    .createdAt(Instant.now())
                    .build();

            commentVoteRepository.save(newVote);
            incrementCommentVoteCount(comment, voteType);
            log.debug("Added new vote to comment {}", commentId);
        }

        commentRepository.save(comment);
    }

    // ========================================
    // SOLUTION MARKING
    // ========================================

    /**
     * Marks a comment as the solution to a forum question.
     * Only post author can mark solutions.
     * Only one comment can be marked as solution per post.
     *
     * @param commentId Comment ID to mark as solution
     * @param userId User attempting to mark (must be post author)
     * @throws ResourceNotFoundException if comment or post not found
     * @throws UnauthorizedException if user is not post author
     */
    @Transactional
    @CacheEvict(value = {"comments", "posts"}, allEntries = true)
    public void markCommentAsSolution(Long commentId, Long userId) {
        log.info("Marking comment {} as solution by user {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Only post author can mark solution
        if (!post.getAuthorId().equals(userId)) {
            throw new UnauthorizedException("Only post author can mark solutions");
        }

        // Only FORUM_QUESTION posts can have solutions
        if (post.getPostType() != Post.PostType.FORUM_QUESTION) {
            throw new ValidationException("Solutions can only be marked on forum questions");
        }

        // Unmark previous solution (if any)
        commentRepository.updateIsSolutionByPostId(post.getPostId(), false);

        // Mark new solution
        comment.setIsSolution(true);
        commentRepository.save(comment);

        // TODO: Send notification to comment author
        // TODO: Award reputation points to comment author

        log.info("Successfully marked comment {} as solution", commentId);
    }

    // ========================================
    // MODULE PROGRESS RETRIEVAL
    // ========================================

    /**
     * Gets user's progress for a specific module.
     *
     * @param userId User ID
     * @param moduleId Module ID
     * @return ModuleProgressDTO or empty progress if not started
     */
    @Cacheable(value = "moduleProgress", key = "#userId + '-' + #moduleId")
    public ModuleProgressDTO getModuleProgress(Long userId, Long moduleId) {
        log.debug("Fetching progress for user {} on module {}", userId, moduleId);

        return moduleProgressRepository.findByUserIdAndModuleId(userId, moduleId)
                .map(progress -> ModuleProgressDTO.builder()
                        .progressId(progress.getProgressId())
                        .userId(progress.getUserId())
                        .moduleId(progress.getModuleId())
                        .progressPercentage(progress.getProgressPercentage())
                        .lastPositionSeconds(progress.getLastPositionSeconds())
                        .timeSpentSeconds(progress.getTimeSpentSeconds())
                        .completed(progress.getCompleted())
                        .completedAt(progress.getCompletedAt())
                        .startedAt(progress.getStartedAt())
                        .updatedAt(progress.getUpdatedAt())
                        .build())
                .orElse(ModuleProgressDTO.builder()
                        .userId(userId)
                        .moduleId(moduleId)
                        .progressPercentage(0)
                        .lastPositionSeconds(0)
                        .timeSpentSeconds(0)
                        .completed(false)
                        .build());
    }

    /**
     * Gets all progress records for a user.
     *
     * @param userId User ID
     * @return List of progress DTOs
     */
    @Cacheable(value = "moduleProgress", key = "'user-' + #userId")
    public List<ModuleProgressDTO> getUserProgress(Long userId) {
        log.debug("Fetching all progress for user {}", userId);

        return moduleProgressRepository.findByUserId(userId)
                .stream()
                .map(progress -> ModuleProgressDTO.builder()
                        .progressId(progress.getProgressId())
                        .userId(progress.getUserId())
                        .moduleId(progress.getModuleId())
                        .progressPercentage(progress.getProgressPercentage())
                        .lastPositionSeconds(progress.getLastPositionSeconds())
                        .timeSpentSeconds(progress.getTimeSpentSeconds())
                        .completed(progress.getCompleted())
                        .completedAt(progress.getCompletedAt())
                        .startedAt(progress.getStartedAt())
                        .updatedAt(progress.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Increments vote count on comment.
     */
    private void incrementCommentVoteCount(Comment comment, Post.VoteType voteType) {
        if (voteType == Post.VoteType.UPVOTE) {
            comment.setUpvotesCount(comment.getUpvotesCount() + 1);
        } else {
            comment.setDownvotesCount(comment.getDownvotesCount() + 1);
        }
    }

    /**
     * Decrements vote count on comment.
     */
    private void decrementCommentVoteCount(Comment comment, Post.VoteType voteType) {
        if (voteType == Post.VoteType.UPVOTE) {
            comment.setUpvotesCount(Math.max(0, comment.getUpvotesCount() - 1));
        } else {
            comment.setDownvotesCount(Math.max(0, comment.getDownvotesCount() - 1));
        }
    }

    /**
     * Checks if user is a moderator (NGO or Admin).
     * In production, this would query the user service.
     *
     * @param userId User ID to check
     * @return true if user is moderator
     */
    private boolean isUserModerator(Long userId) {
        // TODO: Implement actual role check via User Service Feign client
        // For now, return false (only authors can delete their own comments)
        return false;
    }

    /**
     * Asynchronously increments view count on post.
     * Non-blocking to not impact response time.
     *
     * @param postId Post ID
     */
    @Async
    public void incrementViewCountAsync(Long postId) {
        try {
            postRepository.incrementViewCount(postId);
            log.debug("Incremented view count for post {}", postId);
        } catch (Exception e) {
            log.error("Failed to increment view count for post {}", postId, e);
            // Don't throw - view count is non-critical
        }
    }
}