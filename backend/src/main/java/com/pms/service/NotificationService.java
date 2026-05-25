package com.pms.service;

import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.NotificationResponse;
import com.pms.entity.User;
import com.pms.entity.enums.NotificationType;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    NotificationResponse createNotification(User user, String title, String message, NotificationType type);
    PagedResponse<NotificationResponse> getMyNotifications(Pageable pageable);
    void markAsRead(Long id);
    void markAllAsRead();
}
