package com.youthconnect.content_service.repository;

import com.youthconnect.content_service.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Comment entity operations
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Find all approved and active comments for a post
     */
    List<Comment> findByPostIdAndIsApprovedTrueAndIsActiveTrue(Long postId);

    /**
     * Find comments by author
     */
    List<Comment> findByAuthorId(Long authorId);

    /**
     * Find comments requiring moderation
     */
    Page<Comment> findByIsApprovedFalseAndIsActiveTrue(Pageable pageable);

    /**
     * Unmark all solutions for a post (before marking new solution)
     */
    @Modifying
    @Query("UPDATE Comment c SET c.isSolution = false WHERE c.postId = :postId")
    void updateIsSolutionByPostId(@Param("postId") Long postId, @Param("isSolution") boolean isSolution);
}