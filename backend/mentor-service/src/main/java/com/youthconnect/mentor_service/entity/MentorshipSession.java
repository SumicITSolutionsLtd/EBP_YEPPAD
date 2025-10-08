package com.youthconnect.mentor_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentorship_sessions")
@Data
@NoArgsConstructor
public class MentorshipSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @Column(nullable = false)
    private Long mentorId;

    @Column(nullable = false)
    private Long menteeId;

    @Column(nullable = false)
    private LocalDateTime sessionDatetime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.SCHEDULED;

    private String topic;

    public enum Status { SCHEDULED, COMPLETED, CANCELLED }
}
