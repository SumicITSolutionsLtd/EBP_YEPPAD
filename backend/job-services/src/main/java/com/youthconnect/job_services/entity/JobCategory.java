package com.youthconnect.job_services.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

/**
 * Job Category Entity - FIXED with UUID
 *
 * Represents job industry/sector categories.
 * Examples: Technology, Agriculture, Healthcare, etc.
 *
 * UPDATED: Changed from Long to UUID for primary key
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Entity
@Table(name = "job_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCategory extends BaseEntity {

    /**
     * Primary key using UUID
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "category_id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID categoryId;

    @Column(name = "category_name", nullable = false, unique = true, length = 100)
    private String categoryName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // Soft delete support
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}