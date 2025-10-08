package com.youthconnect.mentor_service.repository;

import com.youthconnect.mentor_service.entity.MentorshipSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MentorshipSessionRepository extends JpaRepository<MentorshipSession, Long> {
    // Finds all sessions where the given userId is either the mentor or the mentee.
    List<MentorshipSession> findByMentorIdOrMenteeId(Long mentorId, Long menteeId);
}
