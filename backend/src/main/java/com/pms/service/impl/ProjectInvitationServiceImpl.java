package com.pms.service.impl;

import com.pms.dto.response.ProjectInvitationResponse;
import com.pms.entity.Project;
import com.pms.entity.ProjectInvitation;
import com.pms.entity.Team;
import com.pms.entity.TeamMember;
import com.pms.entity.User;
import com.pms.entity.enums.NotificationType;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.ProjectInvitationMapper;
import com.pms.repository.ProjectInvitationRepository;
import com.pms.repository.ProjectRepository;
import com.pms.repository.TeamMemberRepository;
import com.pms.repository.TeamRepository;
import com.pms.repository.UserRepository;
import com.pms.service.NotificationService;
import com.pms.service.ProjectInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectInvitationServiceImpl implements ProjectInvitationService {

    private final ProjectInvitationRepository invitationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final NotificationService notificationService;
    private final ProjectInvitationMapper invitationMapper;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional
    public ProjectInvitationResponse inviteUser(Long projectId, Long invitedUserId) {
        User currentUser = getCurrentAuthenticatedUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ADMIN"));
        boolean isCreator = project.getCreatedBy() != null && project.getCreatedBy().getId().equals(currentUser.getId());

        if (!isAdmin && !isCreator) {
            throw new AccessDeniedException("Only the project creator or ADMIN can invite users to this project");
        }

        User invitedUser = userRepository.findById(invitedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + invitedUserId));

        if (project.getTeam() != null && teamMemberRepository.existsByTeamAndUser(project.getTeam(), invitedUser)) {
            throw new BadRequestException("User is already a member of this project's team");
        }

        // Check if there is already an active pending invitation
        var existingInvite = invitationRepository.findByProjectAndInvitedUserAndAcceptedFalse(project, invitedUser);
        if (existingInvite.isPresent() && existingInvite.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            // Re-use or update existing active invitation
            ProjectInvitation invite = existingInvite.get();
            // Just refresh token and expiry
            invite.setToken(UUID.randomUUID().toString());
            invite.setExpiresAt(LocalDateTime.now().plusDays(7));
            ProjectInvitation updatedInvite = invitationRepository.save(invite);
            
            notificationService.createNotification(
                    invitedUser,
                    "Project Invitation",
                    "You have been invited to join the project: " + project.getTitle(),
                    NotificationType.TEAM_INVITATION
            );
            
            return invitationMapper.toResponse(updatedInvite);
        }

        ProjectInvitation invitation = ProjectInvitation.builder()
                .project(project)
                .invitedUser(invitedUser)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .accepted(false)
                .build();

        ProjectInvitation savedInvitation = invitationRepository.save(invitation);

        notificationService.createNotification(
                invitedUser,
                "Project Invitation",
                "You have been invited to join the project: " + project.getTitle(),
                NotificationType.TEAM_INVITATION
        );

        return invitationMapper.toResponse(savedInvitation);
    }

    @Override
    @Transactional
    public ProjectInvitationResponse acceptInvitation(String token) {
        User currentUser = getCurrentAuthenticatedUser();
        ProjectInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with token: " + token));

        if (invitation.getAccepted()) {
            throw new BadRequestException("This invitation has already been accepted");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This invitation has expired");
        }

        if (!invitation.getInvitedUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("This invitation was not sent to you");
        }

        Project project = invitation.getProject();
        Team team = project.getTeam();

        if (team == null) {
            // Automatically create team for the project
            team = Team.builder()
                    .name(project.getTitle() + " Team")
                    .description("Automatically created team for project: " + project.getTitle())
                    .createdBy(project.getCreatedBy())
                    .build();
            team = teamRepository.save(team);
            project.setTeam(team);
            projectRepository.save(project);

            // Add the project creator as LEADER to the team
            TeamMember creatorMember = TeamMember.builder()
                    .team(team)
                    .user(project.getCreatedBy())
                    .role("LEADER")
                    .joinedAt(LocalDateTime.now())
                    .build();
            teamMemberRepository.save(creatorMember);
        }

        // Add the invited user to the team
        if (!teamMemberRepository.existsByTeamAndUser(team, currentUser)) {
            TeamMember member = TeamMember.builder()
                    .team(team)
                    .user(currentUser)
                    .role("MEMBER")
                    .joinedAt(LocalDateTime.now())
                    .build();
            teamMemberRepository.save(member);
        }

        invitation.setAccepted(true);
        ProjectInvitation acceptedInvitation = invitationRepository.save(invitation);

        return invitationMapper.toResponse(acceptedInvitation);
    }
}
