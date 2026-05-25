package com.pms.mapper;

import com.pms.dto.response.ReportResponse;
import com.pms.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ReportMapper {

    @Mapping(target = "projectId", source = "project.id")
    ReportResponse toResponse(Report report);
}
