package com.youthconnect.content_service.mapper;

import com.youthconnect.content_service.dto.response.CommentDTO;
import com.youthconnect.content_service.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for Comment entity to DTO conversion
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    /**
     * Convert entity to DTO
     * Replies and author details populated in service layer
     */
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "userVote", ignore = true)
    @Mapping(target = "author", ignore = true)
    CommentDTO toDto(Comment entity);

    /**
     * Convert DTO to entity
     */
    @Mapping(target = "isApproved", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(CommentDTO dto);
}