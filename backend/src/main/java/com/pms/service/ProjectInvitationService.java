package com.pms.service;

import com.pms.dto.response.ProjectInvitationResponse;

// OOP — ABSTRACTION: Contract for inviting users to projects and accepting invitations
public interface ProjectInvitationService {
    ProjectInvitationResponse inviteUser(Long projectId, Long invitedUserId);
    ProjectInvitationResponse acceptInvitation(String token);
}
