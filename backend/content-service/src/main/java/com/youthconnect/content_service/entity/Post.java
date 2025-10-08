package com.youthconnect.content_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * JPA Entity representing the 'posts' table.
 * This is for the main community feed where users can ask questions and share stories.
 */
@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private Long authorId; // The user_id of the person who created the post.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content; // Can be text or a URL to an audio file.

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum PostType {
        FORUM_QUESTION, SUCCESS_STORY, ARTICLE, AUDIO_QUESTION
    }
}