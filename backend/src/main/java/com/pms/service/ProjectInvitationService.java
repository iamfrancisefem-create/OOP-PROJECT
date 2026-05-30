package com.pms.service;

import com.pms.dto.response.ProjectInvitationResponse;

public interface ProjectInvitationService {
    ProjectInvitationResponse inviteUser(Long projectId, Long invitedUserId);
    ProjectInvitationResponse acceptInvitation(String token);
}
