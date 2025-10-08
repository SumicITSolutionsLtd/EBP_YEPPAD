package com.youthconnect.mentor_service.controller;

import com.youthconnect.mentor_service.dto.ReviewRequest;
import com.youthconnect.mentor_service.dto.SessionRequest;
import com.youthconnect.mentor_service.entity.MentorshipSession;
import com.youthconnect.mentor_service.entity.Review;
import com.youthconnect.mentor_service.service.MentorshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mentorship")
@RequiredArgsConstructor
public class MentorshipController {

    private final MentorshipService mentorshipService;

    @PostMapping("/sessions")
    public ResponseEntity<MentorshipSession> bookSession(@RequestBody SessionRequest request) {
        // In a real app, you would get menteeId from the token and validate it.
        MentorshipSession session = mentorshipService.bookSession(request);
        return new ResponseEntity<>(session, HttpStatus.CREATED);
    }

    @GetMapping("/sessions/my-sessions")
    public ResponseEntity<List<MentorshipSession>> getMySessions(@RequestHeader("X-User-Id") Long userId) {
        // This custom header would ideally be added by the API Gateway after reading the JWT.
        // For the hackathon, we send it from Postman.
        List<MentorshipSession> sessions = mentorshipService.getMySessions(userId);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/reviews")
    public ResponseEntity<Review> submitReview(@RequestBody ReviewRequest request) {
        Review review = mentorshipService.submitReview(request);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }
}