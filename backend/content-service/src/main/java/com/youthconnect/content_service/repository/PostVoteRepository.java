package com.youthconnect.content_service.repository;

import com.youthconnect.content_service.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PostVote entity operations
 */
@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Long> {

    /**
     * Find user's vote on a post (returns null if not voted)
     */
    PostVote findByPostIdAndUserId(Long postId, Long userId);

    /**
     * Find all votes by a user
     */
    List<PostVote> findByUserId(Long userId);

    /**
     * Delete a specific vote
     */
    void deleteByPostIdAndUserId(Long postId, Long userId);
}