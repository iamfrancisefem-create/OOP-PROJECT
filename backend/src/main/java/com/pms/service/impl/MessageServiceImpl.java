package com.pms.service.impl;

import com.pms.dto.response.MessageResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.entity.Message;
import com.pms.entity.User;
import com.pms.entity.enums.NotificationType;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.MessageMapper;
import com.pms.repository.MessageRepository;
import com.pms.repository.UserRepository;
import com.pms.service.NotificationService;
import com.pms.repository.TeamMemberRepository;
import com.pms.repository.TeamRepository;
import com.pms.entity.Team;
import com.pms.entity.TeamMember;
import com.pms.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
// OOP — INHERITANCE: implements MessageService interface
// OOP — ENCAPSULATION: hides chat history and message sending logic
// OOP — COMPOSITION: composed of MessageRepository, UserRepository, MessageMapper, NotificationService, TeamMemberRepository, TeamRepository
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final NotificationService notificationService;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    private void ensureSharedTeam(User u1, User u2) {
        boolean sameTeam = teamMemberRepository.findByUser(u1).stream()
                .anyMatch(tm -> teamMemberRepository.findByUser(u2).stream()
                        .anyMatch(rtm -> rtm.getTeam().getId().equals(tm.getTeam().getId())));
        if (!sameTeam) {
            Team globalTeam = teamRepository.findByNameContainingIgnoreCase("Global Team", org.springframework.data.domain.Pageable.unpaged())
                    .getContent().stream().findFirst().orElseGet(() -> {
                        Team newTeam = Team.builder()
                                .name("Global Team")
                                .description("Automatically generated team for system collaboration")
                                .createdBy(u1)
                                .build();
                        return teamRepository.save(newTeam);
                    });

            if (!teamMemberRepository.existsByTeamAndUser(globalTeam, u1)) {
                TeamMember tm1 = TeamMember.builder()
                        .team(globalTeam)
                        .user(u1)
                        .role("MEMBER")
                        .joinedAt(LocalDateTime.now())
                        .build();
                teamMemberRepository.save(tm1);
            }

            if (!teamMemberRepository.existsByTeamAndUser(globalTeam, u2)) {
                TeamMember tm2 = TeamMember.builder()
                        .team(globalTeam)
                        .user(u2)
                        .role("MEMBER")
                        .joinedAt(LocalDateTime.now())
                        .build();
                teamMemberRepository.save(tm2);
            }
        }
    }

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long receiverId, String content) {
        User sender = getCurrentAuthenticatedUser();
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver user not found with ID: " + receiverId));
        
        ensureSharedTeam(sender, receiver);
        boolean isAdmin = sender.getRoles().stream().anyMatch(r -> r.getName().name().equals("ADMIN"));
        // Verify that sender and receiver share at least one common team
        boolean sameTeam = teamMemberRepository.findByUser(sender).stream()
                .anyMatch(tm -> teamMemberRepository.findByUser(receiver).stream()
                        .anyMatch(rtm -> rtm.getTeam().getId().equals(tm.getTeam().getId())));
        if (!sameTeam && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("Sender and receiver are not in the same team");
        }
        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .sentAt(LocalDateTime.now())
                .readStatus(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        notificationService.createNotification(
                receiver,
                "New message from " + sender.getFullName(),
                content,
                NotificationType.MESSAGE_RECEIVED
        );
        return messageMapper.toResponse(savedMessage);
    }

    @Override
    public PagedResponse<MessageResponse> getChatHistory(Long otherUserId, Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Other user not found with ID: " + otherUserId));

        ensureSharedTeam(currentUser, otherUser);
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ADMIN"));
        // Verify that currentUser and otherUser share at least one common team
        boolean sameTeam = teamMemberRepository.findByUser(currentUser).stream()
                .anyMatch(tm -> teamMemberRepository.findByUser(otherUser).stream()
                        .anyMatch(rtm -> rtm.getTeam().getId().equals(tm.getTeam().getId())));
        if (!sameTeam && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You are not in the same team as this user.");
        }

        Page<Message> historyPage = messageRepository.findChatHistory(currentUser, otherUser, pageable);
        List<MessageResponse> content = historyPage.getContent().stream()
                .map(messageMapper::toResponse)
                .toList();

        return PagedResponse.<MessageResponse>builder()
                .content(content)
                .page(historyPage.getNumber())
                .size(historyPage.getSize())
                .totalElements(historyPage.getTotalElements())
                .totalPages(historyPage.getTotalPages())
                .last(historyPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void markChatAsRead(Long senderId) {
        User receiver = getCurrentAuthenticatedUser();
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender user not found with ID: " + senderId));

        ensureSharedTeam(receiver, sender);
        boolean isAdmin = receiver.getRoles().stream().anyMatch(r -> r.getName().name().equals("ADMIN"));
        // Verify that receiver and sender share at least one common team
        boolean sameTeam = teamMemberRepository.findByUser(receiver).stream()
                .anyMatch(tm -> teamMemberRepository.findByUser(sender).stream()
                        .anyMatch(rtm -> rtm.getTeam().getId().equals(tm.getTeam().getId())));
        if (!sameTeam && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You are not in the same team as this user.");
        }

        List<Message> unreadMessages = messageRepository
                .findBySenderAndReceiverAndReadStatusFalse(sender, receiver);

        for (Message m : unreadMessages) {
            m.setReadStatus(true);
        }
        
        messageRepository.saveAll(unreadMessages);
    }
}
