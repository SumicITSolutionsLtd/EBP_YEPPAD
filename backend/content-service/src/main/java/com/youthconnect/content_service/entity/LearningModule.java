package com.youthconnect.content_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * JPA Entity representing the 'learning_modules' table.
 * This class is a direct mapping of the database table and includes all fields
 * and validation constraints that must be enforced at the database level.
 */
@Entity
@Table(name = "learning_modules")
@Data // Lombok: Generates getters, setters, toString, etc.
@NoArgsConstructor // Lombok: Generates a no-argument constructor, required by JPA.
public class LearningModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long moduleId;

    @NotBlank(message = "Title key cannot be blank")
    @Size(max = 100)
    @Column(unique = true, nullable = false, length = 100)
    private String titleKey;

    @NotBlank(message = "Description key cannot be blank")
    @Size(max = 150)
    @Column(unique = true, nullable = false, length = 150)
    private String descriptionKey;

    @NotNull(message = "Content type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentType contentType;

    @Column(length = 500)
    private String audioUrlEn;

    @Column(length = 500)
    private String audioUrlLg;

    @Column(length = 500)
    private String audioUrlLur;

    @Column(length = 500)
    private String audioUrlLgb;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Enumeration for the types of learning content. Stored as a string in the database.
     */
    public enum ContentType {
        AUDIO("Audio Content"),
        VIDEO("Video Content"),
        TEXT("Text Content");

        private final String displayName;

        ContentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}