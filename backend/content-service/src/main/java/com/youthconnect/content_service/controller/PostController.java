package com.youthconnect.content_service.controller;

import com.youthconnect.content_service.dto.PostDTO;
import com.youthconnect.content_service.entity.Post;
import com.youthconnect.content_service.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class PostController {

    private final ContentService contentService;

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        return ResponseEntity.ok(contentService.getAllPosts());
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody Post post) {
        PostDTO createdPost = contentService.createPost(post);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }
}
