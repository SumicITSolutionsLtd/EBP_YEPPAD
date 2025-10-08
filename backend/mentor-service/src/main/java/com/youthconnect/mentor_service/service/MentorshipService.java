package com.youthconnect.mentor_service.service;

import com.youthconnect.mentor_service.dto.ReviewRequest;
import com.youthconnect.mentor_service.dto.SessionRequest;
import com.youthconnect.mentor_service.entity.MentorshipSession;
import com.youthconnect.mentor_service.entity.Review;
import com.youthconnect.mentor_service.repository.MentorshipSessionRepository;
import com.youthconnect.mentor_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorshipService {

    private final MentorshipSessionRepository sessionRepository;
    private final ReviewRepository reviewRepository;

    public MentorshipSession bookSession(SessionRequest request) {
        MentorshipSession newSession = new MentorshipSession();
        newSession.setMentorId(request.getMentorId());
        newSession.setMenteeId(request.getMenteeId());
        newSession.setSessionDatetime(request.getSessionDatetime());
        newSession.setTopic(request.getTopic());
        return sessionRepository.save(newSession);
    }

    public List<MentorshipSession> getMySessions(Long userId) {
        return sessionRepository.findByMentorIdOrMenteeId(userId, userId);
    }

    public Review submitReview(ReviewRequest request) {
        Review newReview = new Review();
        newReview.setReviewerId(request.getReviewerId());
        newReview.setRevieweeId(request.getRevieweeId());
        newReview.setRating(request.getRating());
        newReview.setComment(request.getComment());
        return reviewRepository.save(newReview);
    }
}