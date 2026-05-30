package com.pms.controller;

import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.ProjectInvitationResponse;
import com.pms.service.ProjectInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectInvitationController {

    private final ProjectInvitationService invitationService;

    @PostMapping("/{projectId}/invite")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<ProjectInvitationResponse>> inviteUser(
            @PathVariable Long projectId,
            @RequestParam Long invitedUserId
    ) {
        ProjectInvitationResponse response = invitationService.inviteUser(projectId, invitedUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invitation created successfully.", response));
    }

    @PostMapping("/invitations/accept")
    public ResponseEntity<ApiResponse<ProjectInvitationResponse>> acceptInvitation(
            @RequestParam String token
    ) {
        ProjectInvitationResponse response = invitationService.acceptInvitation(token);
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted successfully.", response));
    }
}
