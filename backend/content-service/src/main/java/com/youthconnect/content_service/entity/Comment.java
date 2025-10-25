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
 * Comment entity for threaded discussions on posts
 */
@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "comment_text", columnDefinition = "TEXT", nullable = false)
    private String commentText;

    // Threading support
    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "thread_depth", nullable = false)
    private Integer threadDepth = 0;

    // Engagement
    @Column(name = "upvotes_count", nullable = false)
    private Integer upvotesCount = 0;

    @Column(name = "downvotes_count", nullable = false)
    private Integer downvotesCount = 0;

    @Column(name = "is_solution", nullable = false)
    private Boolean isSolution = false;

    // Moderation
    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = true;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Helper method to get thread depth
    public Integer getThreadDepth() {
        return threadDepth != null ? threadDepth : 0;
    }
}