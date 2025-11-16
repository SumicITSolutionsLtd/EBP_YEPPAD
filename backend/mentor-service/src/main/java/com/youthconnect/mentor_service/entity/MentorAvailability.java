package com.youthconnect.mentor_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ============================================================================
 * MENTOR AVAILABILITY ENTITY
 * ============================================================================
 * Represents mentor's weekly recurring availability schedule.
 * @author Douglas Kings Kato
 * @since 2025-11-07
 * ============================================================================
 */
@Entity
@Table(name = "mentor_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorAvailability {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "availability_id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID availabilityId;

    @Column(name = "mentor_id", nullable = false, columnDefinition = "UUID")
    private UUID mentorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public int getDurationMinutes() {
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }

    public boolean isTimeAvailable(LocalTime time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public boolean overlapsWith(MentorAvailability other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        return (this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime));
    }
}