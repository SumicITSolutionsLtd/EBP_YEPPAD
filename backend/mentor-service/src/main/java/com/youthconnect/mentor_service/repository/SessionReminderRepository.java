package com.youthconnect.mentor_service.repository;

import com.youthconnect.mentor_service.entity.SessionReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionReminderRepository extends JpaRepository<SessionReminder, Long> {

    List<SessionReminder> findBySessionId(Long sessionId);

    boolean existsBySessionIdAndReminderType(
            Long sessionId,
            SessionReminder.ReminderType reminderType
    );

    List<SessionReminder> findByScheduledTimeBeforeAndSentToMentorFalse(LocalDateTime time);

    List<SessionReminder> findByScheduledTimeBeforeAndSentToMenteeFalse(LocalDateTime time);
}