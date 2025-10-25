package com.youthconnect.content_service.mapper;

import com.youthconnect.content_service.dto.response.PostDTO;
import com.youthconnect.content_service.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for Post entity to DTO conversion
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostMapper {

    /**
     * Convert entity to DTO
     * Author details and user vote populated in service layer
     */
    @Mapping(target = "userVote", ignore = true)
    @Mapping(target = "author", ignore = true)
    PostDTO toDto(Post entity);

    /**
     * Convert DTO to entity
     */
    @Mapping(target = "moderationNotes", ignore = true)
    @Mapping(target = "moderatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Post toEntity(PostDTO dto);
}