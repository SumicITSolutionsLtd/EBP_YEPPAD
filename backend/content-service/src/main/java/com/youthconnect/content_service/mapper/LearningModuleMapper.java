package com.youthconnect.content_service.mapper;

import com.youthconnect.content_service.dto.response.LearningModuleDTO;
import com.youthconnect.content_service.entity.LearningModule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for LearningModule entity to DTO conversion
 * Automatically generates implementation at compile time
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LearningModuleMapper {

    /**
     * Convert entity to DTO
     * Audio URL resolution is handled in service layer
     */
    @Mapping(target = "audioUrl", ignore = true)
    @Mapping(target = "languageCode", ignore = true)
    @Mapping(target = "hasMultipleLanguages", ignore = true)
    @Mapping(target = "progress", ignore = true)
    @Mapping(target = "viewsCount", ignore = true)
    @Mapping(target = "completionsCount", ignore = true)
    LearningModuleDTO toDto(LearningModule entity);

    /**
     * Convert DTO to entity
     */
    LearningModule toEntity(LearningModuleDTO dto);
}