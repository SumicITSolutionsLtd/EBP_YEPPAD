package com.youthconnect.content_service.service;

import com.youthconnect.content_service.dto.response.LearningModuleDTO;
import com.youthconnect.content_service.entity.LearningModule;
import com.youthconnect.content_service.repository.LearningModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service layer for Learning Module business logic and operations.
 *
 * <p>This service acts as the core business logic layer, orchestrating interactions
 * between the controller and repository layers. It encapsulates the multi-language
 * content delivery logic and provides a clean API for learning module operations.</p>
 *
 * <p><strong>Key Responsibilities:</strong></p>
 * <ul>
 *   <li><strong>Language Resolution:</strong> Determines appropriate audio content
 *       based on client language preferences</li>
 *   <li><strong>Entity-DTO Mapping:</strong> Transforms database entities into
 *       API-optimized DTOs</li>
 *   <li><strong>Business Validation:</strong> Enforces business rules and data integrity</li>
 *   <li><strong>Transaction Management:</strong> Ensures data consistency across operations</li>
 * </ul>
 *
 * <p><strong>Supported Languages:</strong></p>
 * <ul>
 *   <li><code>en</code> - English (default/fallback)</li>
 *   <li><code>lg</code> - Luganda (Central Uganda)</li>
 *   <li><code>lur</code> - Luo (Northern Uganda)</li>
 *   <li><code>lgb</code> - Lugbara (West Nile region)</li>
 * </ul>
 *
 * @author YouthConnect Development Team
 * @version 1.0
 * @since 2025-01-01
 */
@Service
@Slf4j // Lombok annotation for logging
@RequiredArgsConstructor // Lombok annotation for constructor injection of final fields
@Transactional(readOnly = true) // All methods are read-only by default
public class LearningModuleService {

    /**
     * Repository for database operations on learning modules.
     * Injected via constructor to ensure immutability and testability.
     */
    private final LearningModuleRepository learningModuleRepository;

    /**
     * Set of supported language codes for validation and fallback logic.
     */
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "lg", "lur", "lgb");

    /**
     * Default language code used when requested language is not supported or available.
     * This ensures there's always a fallback for content delivery.
     */
    private static final String DEFAULT_LANGUAGE = "en";

    /**
     * Creates and persists a new learning module to the database.
     *
     * <p>This method handles the creation of new learning content, including
     * validation of business rules and duplicate prevention based on title keys.
     * It operates within a transaction to ensure data consistency.</p>
     *
     * @param module The LearningModule entity to be created and saved.
     * @return The saved LearningModule entity with generated ID and timestamps.
     * @throws IllegalArgumentException if module is null, has a blank title/description key,
     *                                  or is missing English audio content.
     * @throws IllegalStateException if a module with the same title key already exists.
     */
    @Transactional // This method needs its own read-write transaction
    public LearningModule createModule(LearningModule module) {
        log.info("Attempting to create new learning module with title key: {}", module.getTitleKey());

        // Basic input validation
        if (module == null) {
            throw new IllegalArgumentException("Learning module data cannot be null.");
        }
        if (module.getTitleKey() == null || module.getTitleKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Module title key cannot be blank.");
        }
        if (module.getDescriptionKey() == null || module.getDescriptionKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Module description key cannot be blank.");
        }

        // Validate that the title key is unique before saving
        if (learningModuleRepository.existsByTitleKey(module.getTitleKey())) {
            log.warn("Failed to create module: Duplicate title key '{}'", module.getTitleKey());
            throw new IllegalStateException( // Changed from IllegalArgumentException for clarity of error type
                    "A learning module with title key '" + module.getTitleKey() + "' already exists");
        }

        // Validate that at least English content is provided, as it's the fallback language
        if (module.getAudioUrlEn() == null || module.getAudioUrlEn().trim().isEmpty()) {
            log.warn("Failed to create module '{}': English audio content is required.", module.getTitleKey());
            throw new IllegalArgumentException("English audio content (audioUrlEn) is required for all modules.");
        }

        // Save the module to the database
        LearningModule savedModule = learningModuleRepository.save(module);
        log.info("Successfully created learning module with ID: {} and title key: '{}'",
                savedModule.getModuleId(), savedModule.getTitleKey());

        return savedModule;
    }

    /**
     * Retrieves all learning modules and transforms them into language-specific DTOs.
     *
     * <p>This method is the core of the multi-language audio feature. It fetches all
     * available modules and, for each module, selects the appropriate audio URL
     * based on the provided {@code languageCode}.</p>
     *
     * @param languageCode The two-letter code for the desired language (e.g., "en", "lg", "lur", "lgb").
     *                     If the code is not supported or content is unavailable, it defaults to English.
     * @return A list of {@link LearningModuleDTO}s, each containing a single, language-resolved audio URL.
     */
    public List<LearningModuleDTO> getModulesByLanguage(String languageCode) {
        log.debug("Fetching learning modules for language code: {}", languageCode);

        // 1. Fetch all module entities from the database.
        // The repository method 'findAll()' is efficient for small-to-medium datasets.
        List<LearningModule> allModules = learningModuleRepository.findAll();

        // 2. Use Java Streams to process the list and convert each entity to a DTO.
        // This is a concise and functional way to transform collections.
        List<LearningModuleDTO> dtoList = allModules.stream()
                .map(module -> convertToDto(module, languageCode)) // Convert each module
                .collect(Collectors.toList()); // Collect into a new list

        log.info("Found {} learning modules for language code: {}", dtoList.size(), languageCode);
        return dtoList;
    }

    /**
     * A private helper method to convert a single {@link LearningModule} Entity into a {@link LearningModuleDTO}.
     * This method contains the crucial logic to select the correct audio URL based on the requested language.
     *
     * @param module The database entity.
     * @param languageCode The desired language code (e.g., "en", "lg", "lur", "lgb").
     * @return A DTO ready to be sent to the frontend.
     */
    private LearningModuleDTO convertToDto(LearningModule module, String languageCode) {
        LearningModuleDTO dto = new LearningModuleDTO();
        dto.setModuleId(module.getModuleId());
        dto.setTitleKey(module.getTitleKey());
        dto.setDescriptionKey(module.getDescriptionKey());
        dto.setContentType(module.getContentType()); // Set content type from entity

        // *** THE CORE MULTI-LANGUAGE RESOLUTION LOGIC ***
        // Select the correct audio URL based on the provided language code.
        String resolvedAudioUrl;
        switch (languageCode.toLowerCase()) {
            case "lg":
                resolvedAudioUrl = module.getAudioUrlLg();
                break;
            case "lur":
                resolvedAudioUrl = module.getAudioUrlLur();
                break;
            case "lgb":
                resolvedAudioUrl = module.getAudioUrlLgb();
                break;
            default: // Defaults to "en" if no match or if languageCode is "en"
                resolvedAudioUrl = module.getAudioUrlEn();
                break;
        }

        // Always fall back to the English URL if the specific language URL is null or empty.
        // This ensures the frontend always has a playable audio file.
        dto.setAudioUrl((resolvedAudioUrl != null && !resolvedAudioUrl.trim().isEmpty())
                ? resolvedAudioUrl
                : module.getAudioUrlEn());

        // Indicate to the frontend which language's content is being sent.
        dto.setLanguageCode(languageCode);

        // Determine if the module has multiple language options available
        boolean hasMultiple = (module.getAudioUrlEn() != null && !module.getAudioUrlEn().trim().isEmpty()) &&
                ((module.getAudioUrlLg() != null && !module.getAudioUrlLg().trim().isEmpty()) ||
                        (module.getAudioUrlLur() != null && !module.getAudioUrlLur().trim().isEmpty()) ||
                        (module.getAudioUrlLgb() != null && !module.getAudioUrlLgb().trim().isEmpty()));
        dto.setHasMultipleLanguages(hasMultiple);

        // Set creation timestamp if needed (assuming your entity has it and Lombok populates it)
        // dto.setCreatedAt(module.getCreatedAt());

        return dto;
    }
}

