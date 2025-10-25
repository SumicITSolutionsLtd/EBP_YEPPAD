package com.youthconnect.content_service.mapper;

import com.youthconnect.content_service.dto.response.ModuleProgressDTO;
import com.youthconnect.content_service.entity.ModuleProgress;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for ModuleProgress entity to DTO conversion
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ModuleProgressMapper {

    /**
     * Convert entity to DTO
     */
    ModuleProgressDTO toDto(ModuleProgress entity);

    /**
     * Convert DTO to entity
     */
    ModuleProgress toEntity(ModuleProgressDTO dto);
}