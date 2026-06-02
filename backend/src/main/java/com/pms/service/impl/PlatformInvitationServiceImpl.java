package com.pms.service.impl;

import com.pms.dto.request.CreateInviteRequest;
import com.pms.dto.response.InviteResponse;
import com.pms.entity.PlatformInvitation;
import com.pms.entity.Role;
import com.pms.entity.User;
import com.pms.entity.enums.RoleName;
import com.pms.exception.BadRequestException;
import com.pms.exception.DuplicateResourceException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.PlatformInvitationRepository;
import com.pms.repository.RoleRepository;
import com.pms.repository.UserRepository;
import com.pms.service.PlatformInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformInvitationServiceImpl implements PlatformInvitationService {

    @Value("${app.invite-link-base:https://oop-project-tau.vercel.app}")
    private String inviteLinkBase;

    private final PlatformInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional
    public InviteResponse createInvite(CreateInviteRequest request) {
        User currentUser = getCurrentAuthenticatedUser();

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with this email already exists");
        }

        invitationRepository.findByEmailAndAcceptedFalse(request.getEmail()).ifPresent(invite -> {
            throw new DuplicateResourceException("A pending invitation already exists for this email");
        });

        RoleName roleName;
        try {
            roleName = RoleName.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.getRole());
        }

        String token = UUID.randomUUID().toString();

        PlatformInvitation invitation = PlatformInvitation.builder()
                .email(request.getEmail())
                .role(roleName)
                .token(token)
                .invitedBy(currentUser)
                .accepted(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        PlatformInvitation saved = invitationRepository.save(invitation);

        String inviteLink = inviteLinkBase + "/pages/register.html?invite=" + token;
        sendInviteEmail(request.getEmail(), inviteLink, roleName.name());

        log.info("Platform invitation created for {} with role {} by {}", request.getEmail(), roleName, currentUser.getEmail());

        return toResponse(saved, inviteLink);
    }

    @Override
    @Transactional
    public InviteResponse acceptInvite(String token) {
        PlatformInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found or has expired"));

        if (invitation.getAccepted()) {
            throw new BadRequestException("This invitation has already been accepted");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This invitation has expired");
        }

        invitation.setAccepted(true);
        PlatformInvitation saved = invitationRepository.save(invitation);

        log.info("Platform invitation accepted for {}", saved.getEmail());
        return toResponse(saved, null);
    }

    @Override
    public InviteResponse validateInvite(String token) {
        PlatformInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found or has expired"));

        if (invitation.getAccepted()) {
            throw new BadRequestException("This invitation has already been accepted");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This invitation has expired");
        }

        return toResponse(invitation, null);
    }

    @Override
    public List<InviteResponse> getPendingInvites() {
        return invitationRepository.findByAcceptedFalseOrderByCreatedAtDesc().stream()
                .map(invite -> toResponse(invite, null))
                .toList();
    }

    @Override
    @Transactional
    public void cancelInvite(Long id) {
        PlatformInvitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with ID: " + id));
        invitationRepository.delete(invitation);
        log.info("Platform invitation {} cancelled", id);
    }

    private InviteResponse toResponse(PlatformInvitation invitation, String inviteLink) {
        return InviteResponse.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .role(invitation.getRole().name())
                .token(invitation.getToken())
                .accepted(invitation.getAccepted())
                .invitedBy(invitation.getInvitedBy().getFullName())
                .expiresAt(invitation.getExpiresAt())
                .inviteLink(inviteLink)
                .createdAt(invitation.getCreatedAt())
                .build();
    }

    private void sendInviteEmail(String to, String inviteLink, String role) {
        try {
            if (mailSender == null) {
                log.info("Mail sender not configured. Invite link for {}:\n{}", to, inviteLink);
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("You're invited to join ProjectFlow");
            message.setText("Hello,\n\nYou have been invited to join ProjectFlow as " + role + ".\n\nClick the link below to accept the invitation and create your account:\n" + inviteLink + "\n\nThis link expires in 7 days.\n\nBest regards,\nThe ProjectFlow Team");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send invite email to {}: {}", to, e.getMessage());
        }
    }
}
