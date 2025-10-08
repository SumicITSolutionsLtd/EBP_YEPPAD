package com.youthconnect.content_service.service;

import com.youthconnect.content_service.dto.LearningModuleDTO;
import com.youthconnect.content_service.dto.PostDTO;
import com.youthconnect.content_service.entity.LearningModule;
import com.youthconnect.content_service.entity.Post;
import com.youthconnect.content_service.repository.CommentRepository;
import com.youthconnect.content_service.repository.LearningModuleRepository;
import com.youthconnect.content_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for ALL content-related business logic.
 * This class now handles both Learning Modules and the community Post feed.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {

    private final LearningModuleRepository learningModuleRepository;
    // --- ADD THESE MISSING DEPENDENCIES ---
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // --- EXISTING LEARNING MODULE LOGIC ---
    // (Your existing createModule, getModulesByLanguage, and convertToDto methods for LearningModule go here...)

    // ==========================================================
    // == THIS IS THE FIX: THE MISSING METHODS ARE NOW ADDED ==
    // ==========================================================

    /**
     * Retrieves all posts for the community feed, sorted by most recent.
     * @return A list of PostDTOs.
     */
    public List<PostDTO> getAllPosts() {
        // In a real app, you would add sorting and pagination here.
        return postRepository.findAll()
                .stream()
                .map(this::convertPostToDto)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new post in the community feed.
     * @param post The Post entity to be saved.
     * @return The created post as a DTO.
     */
    @Transactional // Override class-level readOnly=true to allow database writes
    public PostDTO createPost(Post post) {
        // In a real app, you would get the authorId from the JWT token for security.
        Post savedPost = postRepository.save(post);
        return convertPostToDto(savedPost);
    }

    /**
     * A private helper method to map a Post entity to a PostDTO.
     * @param post The entity from the database.
     * @return The corresponding DTO for API responses.
     */
    private PostDTO convertPostToDto(Post post) {
        // In a real app, this method would be more complex. It would make an API call
        // to the user-service to get the author's name and profile picture using the authorId.
        return PostDTO.builder()
                .postId(post.getPostId())
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .build();
    }
}