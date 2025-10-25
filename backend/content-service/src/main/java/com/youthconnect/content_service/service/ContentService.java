package com.youthconnect.content_service.service;

import com.youthconnect.content_service.client.*;
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
 * UNIFIED Content Service - Complete Implementation
 *
 * Handles ALL content-related business logic:
 * 1. Learning Modules (multi-language audio)
 * 2. Community Posts (Reddit-style feed)
 * 3. Comments (threaded discussions)
 * 4. Content Moderation
 * 5. Progress Tracking
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ContentService {

    // ========================================
    // DEPENDENCIES
    // ========================================
    private final LearningModuleRepository learningModuleRepository;
    private final ModuleProgressRepository moduleProgressRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentVoteRepository commentVoteRepository;

    // Mappers
    private final LearningModuleMapper learningModuleMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    // Feign Clients
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationClient;
    private final AnalyticsServiceClient analyticsClient;
    private final FileServiceClient fileServiceClient;

    // ========================================
    // LEARNING MODULE METHODS
    // ========================================

    /**
     * Retrieves all active learning modules with language-specific audio URLs
     *
     * @param languageCode User's preferred language (en, lg, lur, lgb)
     * @return List of LearningModuleDTOs with resolved audio URLs
     */
    @Cacheable(value = "learningModules", key = "#languageCode")
    public List<LearningModuleDTO> getModulesByLanguage(String languageCode) {
        log.debug("Fetching learning modules for language: {}", languageCode);

        List<LearningModule> modules = learningModuleRepository.findAll();

        return modules.stream()
                .map(module -> convertToDto(module, languageCode))
                .collect(Collectors.toList());
    }

    /**
     * Creates a new learning module (ADMIN/NGO only)
     *
     * @param module Learning module entity
     * @return Saved learning module
     */
    @Transactional
    @CacheEvict(value = "learningModules", allEntries = true)
    public LearningModule createModule(LearningModule module) {
        log.info("Creating new learning module: {}", module.getTitleKey());

        validateModule(module);

        LearningModule saved = learningModuleRepository.save(module);

        log.info("Successfully created module with ID: {}", saved.getModuleId());
        return saved;
    }

    /**
     * Updates user's progress on a learning module
     *
     * @param userId User ID
     * @param moduleId Module ID
     * @param request Progress update data
     */
    @Transactional
    @CacheEvict(value = "moduleProgress", key = "#userId + '-' + #moduleId")
    public void updateModuleProgress(Long userId, Long moduleId, UpdateProgressRequest request) {
        log.debug("Updating progress for user {} on module {}", userId, moduleId);

        ModuleProgress progress = moduleProgressRepository
                .findByUserIdAndModuleId(userId, moduleId)
                .orElseGet(() -> {
                    ModuleProgress newProgress = new ModuleProgress();
                    newProgress.setUserId(userId);
                    newProgress.setModuleId(moduleId);
                    newProgress.setStartedAt(Instant.now());
                    return newProgress;
                });

        progress.setProgressPercentage(request.getProgressPercentage());
        progress.setLastPositionSeconds(request.getLastPositionSeconds());
        progress.setUpdatedAt(Instant.now());

        // Check for completion (>= 90% progress)
        if (request.getProgressPercentage() >= 90 && !progress.getCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(Instant.now());

            log.info("User {} completed module {}", userId, moduleId);

            // Trigger certificate generation and notification
            LearningModule module = learningModuleRepository.findById(moduleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

            notificationClient.sendModuleCompletionNotification(userId, module.getTitleKey());
            analyticsClient.trackModuleCompletion(moduleId, userId, progress.getTimeSpentSeconds());
        }

        moduleProgressRepository.save(progress);
    }

    // ========================================
    // COMMUNITY POST METHODS
    // ========================================

    /**
     * Retrieves all approved posts for the community feed
     *
     * @param pageable Pagination parameters
     * @return Page of PostDTOs
     */
    @Cacheable(value = "posts", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<PostDTO> getAllPosts(Pageable pageable) {
        log.debug("Fetching all posts - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Post> posts = postRepository.findByIsApprovedTrueAndIsActiveTrue(pageable);

        return posts.map(this::convertPostToDto);
    }

    /**
     * Retrieves a single post by ID
     *
     * @param postId Post ID
     * @return PostDTO
     */
    @Cacheable(value = "posts", key = "#postId")
    public PostDTO getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        // Increment view count asynchronously
        incrementViewCountAsync(postId);

        return convertPostToDto(post);
    }

    /**
     * Creates a new community post
     *
     * @param request Post creation data
     * @param authorId User ID of author
     * @return Created PostDTO
     */
    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public PostDTO createPost(CreatePostRequest request, Long authorId) {
        log.info("Creating post by user {}: {}", authorId, request.getTitle());

        validatePostRequest(request);

        // Auto-moderation check
        boolean requiresModeration = requiresManualReview(request.getContent());

        Post post = Post.builder()
                .authorId(authorId)
                .postType(request.getPostType())
                .title(request.getTitle())
                .content(request.getContent())
                .isApproved(!requiresModeration)
                .isActive(true)
                .viewsCount(0)
                .upvotesCount(0)
                .downvotesCount(0)
                .commentsCount(0)
                .createdAt(Instant.now())
                .build();

        Post saved = postRepository.save(post);

        if (requiresModeration) {
            log.warn("Post {} flagged for moderation", saved.getPostId());
        }

        log.info("Successfully created post with ID: {}", saved.getPostId());
        return convertPostToDto(saved);
    }

    // ========================================
    // COMMENT METHODS
    // ========================================

    /**
     * Retrieves all comments for a post (threaded)
     *
     * @param postId Post ID
     * @return List of CommentDTOs
     */
    @Cacheable(value = "comments", key = "#postId")
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository
                .findByPostIdAndIsApprovedTrueAndIsActiveTrue(postId);

        return comments.stream()
                .map(this::convertCommentToDto)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new comment on a post
     *
     * @param request Comment creation data
     * @param authorId User ID of comment author
     * @return Created CommentDTO
     */
    @Transactional
    @CacheEvict(value = {"comments", "posts"}, allEntries = true)
    public CommentDTO createComment(CreateCommentRequest request, Long authorId) {
        log.info("Creating comment by user {} on post {}", authorId, request.getPostId());

        // Validate post exists
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Validate comment length
        if (request.getCommentText().length() < 5) {
            throw new ValidationException("Comment must be at least 5 characters");
        }

        // Calculate thread depth if reply
        int threadDepth = 0;
        if (request.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));

            threadDepth = parent.getThreadDepth() + 1;
            if (threadDepth > 5) {
                throw new ValidationException("Maximum comment depth (5) exceeded");
            }
        }

        // Auto-moderation
        boolean requiresModeration = requiresManualReview(request.getCommentText());

        Comment comment = Comment.builder()
                .postId(request.getPostId())
                .authorId(authorId)
                .commentText(request.getCommentText())
                .parentCommentId(request.getParentCommentId())
                .threadDepth(threadDepth)
                .isApproved(!requiresModeration)
                .isActive(true)
                .upvotesCount(0)
                .downvotesCount(0)
                .isSolution(false)
                .createdAt(Instant.now())
                .build();

        Comment saved = commentRepository.save(comment);

        // Increment post's comment count
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);

        // Send notification to post author
        UserBasicInfoDto commenter = userServiceClient.getUserBasicInfo(authorId);
        notificationClient.sendNewCommentNotification(post.getAuthorId(), post.getTitle(), commenter.getDisplayName());

        log.info("Successfully created comment with ID: {}", saved.getCommentId());
        return convertCommentToDto(saved);
    }

    /**
     * Delete a comment (soft delete)
     *
     * @param commentId Comment ID to delete
     * @param userId User attempting deletion
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
    // VOTING METHODS
    // ========================================

    /**
     * Handles upvote/downvote on a post
     *
     * @param postId Post ID
     * @param userId User ID
     * @param voteType UPVOTE or DOWNVOTE
     */
    @Transactional
    @CacheEvict(value = {"posts", "postVotes"}, allEntries = true)
    public void votePost(Long postId, Long userId, Post.VoteType voteType) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Check for existing vote
        PostVote existingVote = postVoteRepository
                .findByPostIdAndUserId(postId, userId);

        if (existingVote != null) {
            if (existingVote.getVoteType() == voteType) {
                // Remove vote (toggle off)
                postVoteRepository.delete(existingVote);
                decrementVoteCount(post, voteType);
            } else {
                // Change vote
                Post.VoteType oldType = existingVote.getVoteType();
                existingVote.setVoteType(voteType);
                postVoteRepository.save(existingVote);

                // Update counts
                decrementVoteCount(post, oldType);
                incrementVoteCount(post, voteType);
            }
        } else {
            // New vote
            PostVote newVote = PostVote.builder()
                    .postId(postId)
                    .userId(userId)
                    .voteType(voteType)
                    .createdAt(Instant.now())
                    .build();

            postVoteRepository.save(newVote);
            incrementVoteCount(post, voteType);
        }

        postRepository.save(post);
    }

    /**
     * Handles upvote/downvote on a comment
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

    /**
     * Marks a comment as the solution to a forum question
     *
     * @param commentId Comment ID to mark as solution
     * @param userId User attempting to mark (must be post author)
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

        // Notify solution author
        notificationClient.sendSolutionAcceptedNotification(comment.getAuthorId(), post.getTitle());

        log.info("Successfully marked comment {} as solution", commentId);
    }

    // ========================================
    // HELPER METHODS (PRIVATE)
    // ========================================

    /**
     * Converts LearningModule entity to DTO with language-specific audio URL
     */
    private LearningModuleDTO convertToDto(LearningModule module, String languageCode) {
        LearningModuleDTO dto = learningModuleMapper.toDto(module);

        // Resolve audio URL based on language
        String audioUrl = resolveAudioUrl(module, languageCode);
        dto.setAudioUrl(audioUrl);
        dto.setLanguageCode(languageCode);

        // Check if multiple languages available
        boolean hasMultiple = hasMultipleLanguages(module);
        dto.setHasMultipleLanguages(hasMultiple);

        return dto;
    }

    /**
     * Resolves audio URL based on language preference
     * Falls back to English if preferred language not available
     */
    private String resolveAudioUrl(LearningModule module, String languageCode) {
        return switch (languageCode.toLowerCase()) {
            case "lg" -> module.getAudioUrlLg() != null ? module.getAudioUrlLg() : module.getAudioUrlEn();
            case "lur" -> module.getAudioUrlLur() != null ? module.getAudioUrlLur() : module.getAudioUrlEn();
            case "lgb" -> module.getAudioUrlLgb() != null ? module.getAudioUrlLgb() : module.getAudioUrlEn();
            default -> module.getAudioUrlEn();
        };
    }

    /**
     * Checks if module has audio in multiple languages
     */
    private boolean hasMultipleLanguages(LearningModule module) {
        int languageCount = 0;
        if (module.getAudioUrlEn() != null) languageCount++;
        if (module.getAudioUrlLg() != null) languageCount++;
        if (module.getAudioUrlLur() != null) languageCount++;
        if (module.getAudioUrlLgb() != null) languageCount++;
        return languageCount > 1;
    }

    /**
     * Converts Post entity to DTO
     */
    private PostDTO convertPostToDto(Post post) {
        return postMapper.toDto(post);
    }

    /**
     * Converts Comment entity to DTO
     */
    private CommentDTO convertCommentToDto(Comment comment) {
        return commentMapper.toDto(comment);
    }

    /**
     * Validates learning module data
     */
    private void validateModule(LearningModule module) {
        if (module.getTitleKey() == null || module.getTitleKey().isBlank()) {
            throw new ValidationException("Title key is required");
        }

        if (module.getDescriptionKey() == null || module.getDescriptionKey().isBlank()) {
            throw new ValidationException("Description key is required");
        }

        if (module.getAudioUrlEn() == null || module.getAudioUrlEn().isBlank()) {
            throw new ValidationException("English audio URL is required (fallback language)");
        }

        // Check for duplicate title key
        if (learningModuleRepository.existsByTitleKey(module.getTitleKey())) {
            throw new ValidationException("Module with title key already exists: " + module.getTitleKey());
        }
    }

    /**
     * Validates post creation request
     */
    private void validatePostRequest(CreatePostRequest request) {
        if (request.getTitle() == null || request.getTitle().length() < 10) {
            throw new ValidationException("Title must be at least 10 characters");
        }

        if (request.getTitle().length() > 255) {
            throw new ValidationException("Title cannot exceed 255 characters");
        }

        if (request.getContent() == null || request.getContent().length() < 20) {
            throw new ValidationException("Content must be at least 20 characters");
        }

        if (request.getContent().length() > 5000) {
            throw new ValidationException("Content cannot exceed 5000 characters");
        }
    }

    /**
     * Basic auto-moderation check
     * Returns true if content requires manual review
     */
    private boolean requiresManualReview(String content) {
        if (content == null) return false;

        String lowerContent = content.toLowerCase();

        // Check for excessive URLs (spam indicator)
        long urlCount = lowerContent.split("http").length - 1;
        if (urlCount > 2) return true;

        // Check for excessive capitals (shouting)
        long capsCount = content.chars().filter(Character::isUpperCase).count();
        if (capsCount > content.length() * 0.5) return true;

        // Check for phone numbers (spam)
        if (lowerContent.matches(".*\\+?256\\d{9}.*")) return true;

        // Profanity filter would go here
        return false;
    }

    /**
     * Increments view count asynchronously
     */
    @Async
    public void incrementViewCountAsync(Long postId) {
        try {
            postRepository.incrementViewCount(postId);
            log.debug("Incremented view count for post {}", postId);
        } catch (Exception e) {
            log.error("Failed to increment view count for post {}", postId, e);
        }
    }

    /**
     * Increments vote count on post
     */
    private void incrementVoteCount(Post post, Post.VoteType voteType) {
        if (voteType == Post.VoteType.UPVOTE) {
            post.setUpvotesCount(post.getUpvotesCount() + 1);
        } else {
            post.setDownvotesCount(post.getDownvotesCount() + 1);
        }
    }

    /**
     * Decrements vote count on post
     */
    private void decrementVoteCount(Post post, Post.VoteType voteType) {
        if (voteType == Post.VoteType.UPVOTE) {
            post.setUpvotesCount(Math.max(0, post.getUpvotesCount() - 1));
        } else {
            post.setDownvotesCount(Math.max(0, post.getDownvotesCount() - 1));
        }
    }

    /**
     * Increments vote count on comment
     */
    private void incrementCommentVoteCount(Comment comment, Post.VoteType voteType) {
        if (voteType == Post.VoteType.UPVOTE) {
            comment.setUpvotesCount(comment.getUpvotesCount() + 1);
        } else {
            comment.setDownvotesCount(comment.getDownvotesCount() + 1);
        }
    }

    /**
     * Decrements vote count on comment
     */
    private void decrementCommentVoteCount(Comment comment, Post.VoteType voteType) {
        if (voteType == Post.VoteType.UPVOTE) {
            comment.setUpvotesCount(Math.max(0, comment.getUpvotesCount() - 1));
        } else {
            comment.setDownvotesCount(Math.max(0, comment.getDownvotesCount() - 1));
        }
    }

    /**
     * Checks if user is a moderator (NGO or Admin)
     * In production, this would query the user service
     */
    private boolean isUserModerator(Long userId) {
        // TODO: Implement actual role check via User Service Feign client
        // For now, return false (only authors can delete their own comments)
        return false;
    }
}