package com.pms.mapper;

import com.pms.dto.request.TaskRequest;
import com.pms.dto.response.TaskResponse;
import com.pms.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TaskMapper {

    @Mapping(target = "projectId", source = "project.id")
    TaskResponse toResponse(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Task toEntity(TaskRequest request);
}
