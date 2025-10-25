package com.youthconnect.content_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.youthconnect.content_service.entity.LearningModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for Learning Module API responses.
 *
 * <p>This class defines the clean, language-specific data structure that is sent to the frontend.
 * It is a best practice to use DTOs to separate the internal database structure (Entity)
 * from the public API contract, which improves security and flexibility.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // This tells Jackson to ignore any fields that are null in the final JSON output.
public class LearningModuleDTO {

    /**
     * Unique identifier for the learning module.
     */
    private Long moduleId;

    /**
     * Translation key for the module title (e.g., "module.business_intro.title").
     * The frontend uses this key to look up the correct translated text.
     */
    private String titleKey;

    /**
     * Translation key for the module description.
     */
    private String descriptionKey;

    /**
     * The type of content (e.g., AUDIO, VIDEO, TEXT).
     */
    private LearningModule.ContentType contentType;

    /**
     * The single, language-specific audio file URL, resolved by the service layer.
     * This is the core transformation of this DTO.
     */
    private String audioUrl;

    /**
     * A flag to inform the frontend if this module has content in more than one language.
     * This helps the UI decide whether to show language-switching options.
     */
    private Boolean hasMultipleLanguages;

    /**
     * The language code of the content being provided in this DTO (e.g., "en", "lg").
     */
    private String languageCode;

    /**
     * Timestamp for when the module was created, useful for "Recently Added" features.
     */
    private LocalDateTime createdAt;
}