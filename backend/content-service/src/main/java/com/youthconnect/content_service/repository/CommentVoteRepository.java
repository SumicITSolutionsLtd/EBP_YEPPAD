package com.youthconnect.content_service.repository;

import com.youthconnect.content_service.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CommentVote entity operations
 */
@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {

    /**
     * Find user's vote on a comment
     */
    CommentVote findByCommentIdAndUserId(Long commentId, Long userId);

    /**
     * Find all votes by a user
     */
    List<CommentVote> findByUserId(Long userId);

    /**
     * Delete a specific vote
     */
    void deleteByCommentIdAndUserId(Long commentId, Long userId);
}