package com.pms.service.impl;

import com.pms.dto.response.NotificationResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.entity.Notification;
import com.pms.entity.User;
import com.pms.entity.enums.NotificationType;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.NotificationMapper;
import com.pms.repository.NotificationRepository;
import com.pms.repository.UserRepository;
import com.pms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(User user, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .seen(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Created notification for user: {}", user.getEmail());
        
        // Asynchronously mirror as email alert
        sendEmailAlert(user.getEmail(), title, message);

        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    public PagedResponse<NotificationResponse> getMyNotifications(Pageable pageable) {
        User user = getCurrentAuthenticatedUser();
        Page<Notification> page = notificationRepository.findByUserAndSeenFalseOrderByCreatedAtDesc(user, pageable);
        List<NotificationResponse> content = page.getContent().stream()
                .map(notificationMapper::toResponse)
                .toList();

        return PagedResponse.<NotificationResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        notification.setSeen(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User user = getCurrentAuthenticatedUser();
        List<Notification> unread = notificationRepository.findByUserAndSeen(user, false);
        for (Notification n : unread) {
            n.setSeen(true);
        }
        notificationRepository.saveAll(unread);
        log.info("Marked all notifications as read for user: {}", user.getEmail());
    }

    private void sendEmailAlert(String email, String subject, String body) {
        try {
            if (mailSender == null) {
                log.info("Mail sender not configured. Notification alert logged to console: \nSubject: {}\nBody: {}", subject, body);
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send notification email to {}: {}", email, e.getMessage());
        }
    }
}
