package com.youthconnect.content_service.repository;

import com.youthconnect.content_service.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Post entity operations
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Find all approved and active posts (for public feed)
     */
    Page<Post> findByIsApprovedTrueAndIsActiveTrue(Pageable pageable);

    /**
     * Find posts by author
     */
    List<Post> findByAuthorId(Long authorId);

    /**
     * Find posts by type
     */
    Page<Post> findByPostTypeAndIsApprovedTrueAndIsActiveTrue(
            Post.PostType postType, Pageable pageable);

    /**
     * Increment view count (should be called asynchronously)
     */
    @Modifying
    @Query("UPDATE Post p SET p.viewsCount = p.viewsCount + 1 WHERE p.postId = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    /**
     * Find posts requiring moderation
     */
    Page<Post> findByIsApprovedFalseAndIsActiveTrue(Pageable pageable);
}