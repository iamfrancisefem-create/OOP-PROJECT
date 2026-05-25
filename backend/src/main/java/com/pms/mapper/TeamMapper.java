package com.pms.mapper;

import com.pms.dto.request.TeamRequest;
import com.pms.dto.response.TeamResponse;
import com.pms.entity.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TeamMapper {

    TeamResponse toResponse(Team team);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "projects", ignore = true)
    Team toEntity(TeamRequest request);
}
