package com.pms.mapper;

import com.pms.dto.request.MilestoneRequest;
import com.pms.dto.response.MilestoneResponse;
import com.pms.entity.Milestone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MilestoneMapper {

    @Mapping(target = "projectId", source = "project.id")
    MilestoneResponse toResponse(Milestone milestone);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    Milestone toEntity(MilestoneRequest request);
}
