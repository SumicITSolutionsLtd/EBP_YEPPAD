package com.youthconnect.content_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Post entity for community forum
 * Supports multiple post types and engagement tracking
 */
@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 30)
    private PostType postType;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // For audio questions (USSD users)
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    // Moderation fields
    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = true;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "moderation_notes", columnDefinition = "TEXT")
    private String moderationNotes;

    @Column(name = "moderated_by")
    private Long moderatedBy;

    // Engagement metrics
    @Column(name = "views_count", nullable = false)
    private Integer viewsCount = 0;

    @Column(name = "upvotes_count", nullable = false)
    private Integer upvotesCount = 0;

    @Column(name = "downvotes_count", nullable = false)
    private Integer downvotesCount = 0;

    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ========================================
    // ENUMS
    // ========================================

    /**
     * Types of posts in the community
     */
    public enum PostType {
        FORUM_QUESTION,
        SUCCESS_STORY,
        ARTICLE,
        AUDIO_QUESTION
    }

    /**
     * Vote types for posts and comments
     */
    public enum VoteType {
        UPVOTE,
        DOWNVOTE
    }
}