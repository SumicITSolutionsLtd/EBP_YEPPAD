package com.youthconnect.content_service.repository;

import com.youthconnect.content_service.entity.LearningModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository interface for LearningModule entity operations.
 *
 * <p>This repository serves as the data access layer, providing an abstraction
 * over database operations for learning modules. It extends JpaRepository to
 * inherit standard CRUD operations and adds custom query methods for specific
 * business requirements.</p>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li>Automatic CRUD operations via JpaRepository inheritance</li>
 *   <li>Custom finder methods using Spring Data JPA method naming conventions</li>
 *   <li>Type-safe database operations with compile-time checking</li>
 *   <li>Transaction management handled automatically by Spring</li>
 * </ul>
 *
 * @author YouthConnect Development Team
 * @version 1.0
 * @since 2025-01-01
 */
@Repository
public interface LearningModuleRepository extends JpaRepository<LearningModule, Long> {

    /**
     * Finds a learning module by its unique title key identifier.
     *
     * <p>This method is essential for content management operations where
     * modules are identified by their semantic title keys rather than
     * numeric IDs. The title key serves as a human-readable identifier
     * that corresponds to translation keys in the frontend.</p>
     *
     * <p>Spring Data JPA automatically generates the SQL query:
     * {@code SELECT * FROM learning_modules WHERE title_key = ?}</p>
     *
     * @param titleKey the unique string identifier for the module's title
     * @return an Optional containing the found LearningModule, or empty if not found
     * @throws IllegalArgumentException if titleKey is null
     */
    Optional<LearningModule> findByTitleKey(String titleKey);

    /**
     * Finds all learning modules by their content type.
     *
     * <p>This method enables filtering modules based on their media type,
     * which is useful for organizing content and providing type-specific
     * endpoints to the frontend application.</p>
     *
     * @param contentType the type of content (AUDIO, VIDEO, TEXT)
     * @return a list of learning modules matching the specified content type
     */
    List<LearningModule> findByContentType(LearningModule.ContentType contentType);

    /**
     * Checks if a learning module exists with the given title key.
     *
     * <p>This method is optimized for existence checks without loading
     * the entire entity, making it more efficient than findByTitleKey
     * when only validation is needed.</p>
     *
     * @param titleKey the title key to check for existence
     * @return true if a module with the given title key exists, false otherwise
     */
    boolean existsByTitleKey(String titleKey);

    /**
     * Finds all learning modules that have audio content in a specific language.
     *
     * <p>This custom JPQL query enables filtering modules based on the availability
     * of audio content in specific languages, which is crucial for the multi-language
     * audio feature implementation.</p>
     *
     * @param languageCode the language code (en, lg, lur, lgb)
     * @return a list of modules that have non-null audio URLs for the specified language
     */
    @Query("SELECT lm FROM LearningModule lm WHERE " +
            "(:languageCode = 'en' AND lm.audioUrlEn IS NOT NULL) OR " +
            "(:languageCode = 'lg' AND lm.audioUrlLg IS NOT NULL) OR " +
            "(:languageCode = 'lur' AND lm.audioUrlLur IS NOT NULL) OR " +
            "(:languageCode = 'lgb' AND lm.audioUrlLgb IS NOT NULL)")
    List<LearningModule> findModulesWithAudioInLanguage(@Param("languageCode") String languageCode);

    /**
     * Counts the total number of learning modules by content type.
     *
     * <p>This method provides statistical information about content distribution,
     * useful for administrative dashboards and content management insights.</p>
     *
     * @param contentType the content type to count
     * @return the number of modules of the specified content type
     */
    long countByContentType(LearningModule.ContentType contentType);

    /*
     * IMPLEMENTATION NOTES:
     *
     * 1. TRANSACTION MANAGEMENT:
     *    All repository methods are automatically wrapped in read-only transactions
     *    by Spring Data JPA. Write operations (save, delete) use read-write transactions.
     *
     * 2. ERROR HANDLING:
     *    Repository methods may throw DataAccessException and its subclasses.
     *    These should be handled appropriately in the service layer.
     *
     * 3. PERFORMANCE CONSIDERATIONS:
     *    - Use Optional.isPresent() checks to avoid loading entities unnecessarily
     *    - Consider using @Query for complex operations to optimize database queries
     *    - Pagination should be implemented for large result sets using Pageable parameters
     *
     * 4. FUTURE ENHANCEMENTS:
     *    - Full-text search capability would require storing actual content text
     *    - Audit trails could be added using Spring Data JPA Auditing
     *    - Soft delete functionality could be implemented with @Where annotations
     */
}