package com.youthconnect.opportunity_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * JPA Entity representing the 'opportunities' table in the database.
 * This is the core entity for this microservice.
 */
@Entity
@Table(name = "opportunities")
@Data
@NoArgsConstructor
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long opportunityId;

    // This will store the user_id of the NGO or Partner who posted the opportunity.
    // In a microservice architecture, we store the ID rather than a direct object relationship.
    @Column(nullable = false)
    private Long postedById;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityType opportunityType;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'OPEN'")
    private Status status = Status.OPEN;

    private LocalDateTime applicationDeadline;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Enums to match the database schema, providing type safety.
    public enum OpportunityType { GRANT, LOAN, JOB, TRAINING, SKILL_MARKET }
    public enum Status { OPEN, CLOSED, IN_REVIEW }
}