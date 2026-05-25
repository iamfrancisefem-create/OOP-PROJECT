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
import com.pms.service.MessageService;
import com.pms.service.NotificationService;
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
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final NotificationService notificationService;

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

        List<Message> unreadMessages = messageRepository.findAll().stream()
                .filter(m -> m.getSender().getId().equals(sender.getId())
                        && m.getReceiver().getId().equals(receiver.getId())
                        && !m.getReadStatus())
                .toList();

        for (Message m : unreadMessages) {
            m.setReadStatus(true);
        }
        
        messageRepository.saveAll(unreadMessages);
    }
}
