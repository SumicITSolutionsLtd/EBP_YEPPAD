package com.youthconnect.content_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * JPA Entity representing the 'comments' table.
 * Each comment is linked to a parent Post.
 */
@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long authorId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String commentText;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}