package com.pms.mapper;

import com.pms.dto.request.ProjectRequest;
import com.pms.dto.response.ProjectResponse;
import com.pms.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, TeamMapper.class})
public interface ProjectMapper {

    ProjectResponse toResponse(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "milestones", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "progress", ignore = true)
    Project toEntity(ProjectRequest request);
}
