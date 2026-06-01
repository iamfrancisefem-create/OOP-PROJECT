package com.pms.service;

import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.MessageResponse;
import org.springframework.data.domain.Pageable;

// OOP — ABSTRACTION: Contract for messaging between users
public interface MessageService {
    MessageResponse sendMessage(Long receiverId, String content);
    PagedResponse<MessageResponse> getChatHistory(Long otherUserId, Pageable pageable);
    void markChatAsRead(Long senderId);
}
