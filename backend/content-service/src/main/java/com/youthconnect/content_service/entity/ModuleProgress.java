package com.youthconnect.content_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Tracks user progress on learning modules
 */
@Entity
@Table(name = "module_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "module_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage = 0;

    @Column(name = "last_position_seconds", nullable = false)
    private Integer lastPositionSeconds = 0;

    @Column(name = "time_spent_seconds", nullable = false)
    private Integer timeSpentSeconds = 0;

    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Helper method for null safety
    public Boolean getCompleted() {
        return completed != null ? completed : false;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public void setLastPositionSeconds(Integer lastPositionSeconds) {
        this.lastPositionSeconds = lastPositionSeconds;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}