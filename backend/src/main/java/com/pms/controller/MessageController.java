package com.pms.controller;

import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.MessageResponse;
import com.pms.dto.request.MessageRequest;
import com.pms.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody MessageRequest request
    ) {
        MessageResponse response = messageService.sendMessage(request.getReceiverId(), request.getContent());
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully.", response));
    }

    @GetMapping("/chat/{otherUserId}")
    public ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> getChatHistory(
            @PathVariable Long otherUserId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        PagedResponse<MessageResponse> response = messageService.getChatHistory(otherUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Chat history retrieved successfully.", response));
    }

    @PostMapping("/chat/{senderId}/read")
    public ResponseEntity<ApiResponse<Void>> markChatAsRead(@PathVariable Long senderId) {
        messageService.markChatAsRead(senderId);
        return ResponseEntity.ok(ApiResponse.success("Chat marked as read successfully.", null));
    }
}
