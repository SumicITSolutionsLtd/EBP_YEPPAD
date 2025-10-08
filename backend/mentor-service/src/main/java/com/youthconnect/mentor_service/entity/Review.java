package com.youthconnect.mentor_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false)
    private Long reviewerId; // The ID of the user giving the review (mentee)

    @Column(nullable = false)
    private Long revieweeId; // The ID of the user being reviewed (mentor)

    @Column(nullable = false)
    private int rating; // A rating from 1 to 5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
