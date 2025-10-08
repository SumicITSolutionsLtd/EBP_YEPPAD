package com.youthconnect.content_service.controller;


import com.youthconnect.content_service.dto.LearningModuleDTO;
import com.youthconnect.content_service.entity.LearningModule;
import com.youthconnect.content_service.service.LearningModuleService; // <-- Import the new service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller for managing Learning Modules.
 * This class now delegates all business logic to the LearningModuleService.
 */
@RestController
@RequestMapping("/api/learning")
public class LearningModuleController {

    // We no longer inject the repository directly. We inject our service.
    @Autowired
    private LearningModuleService learningModuleService;

    /**
     * Endpoint to get all learning modules, tailored to a specific language.
     * Example Usage: GET /api/learning/modules?lang=lg
     *
     * @param languageCode The language code provided by the frontend as a request parameter.
     *                     Defaults to "en" if not provided.
     * @return A list of LearningModuleDTOs containing the correct audio URL for the requested language.
     */
    @GetMapping("/modules")
    public ResponseEntity<List<LearningModuleDTO>> getAllModules(
            @RequestParam(name = "lang", defaultValue = "en") String languageCode) {

        // Delegate the complex logic to the service layer.
        List<LearningModuleDTO> modules = learningModuleService.getModulesByLanguage(languageCode);
        return ResponseEntity.ok(modules);
    }

    /**
     * Endpoint to create a new learning module.
     * This is an administrative endpoint you can use with Postman to add new content.
     * @param module The raw LearningModule entity sent in the request body.
     * @return The newly created learning module entity as confirmation.
     */
    @PostMapping("/modules")
    public ResponseEntity<LearningModule> createModule(@RequestBody LearningModule module) {
        // Delegate the creation logic to the service layer.
        LearningModule savedModule = learningModuleService.createModule(module);
        return ResponseEntity.ok(savedModule);
    }
}