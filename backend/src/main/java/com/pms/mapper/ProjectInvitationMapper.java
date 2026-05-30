package com.pms.mapper;

import com.pms.dto.response.ProjectInvitationResponse;
import com.pms.entity.ProjectInvitation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProjectMapper.class, UserMapper.class})
public interface ProjectInvitationMapper {

    ProjectInvitationResponse toResponse(ProjectInvitation invitation);
}
