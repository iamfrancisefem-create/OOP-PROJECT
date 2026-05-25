package com.pms.websocket;

import com.pms.dto.request.MessageRequest;
import com.pms.dto.response.MessageResponse;
import com.pms.entity.User;
import com.pms.repository.UserRepository;
import com.pms.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void processMessage(@Payload MessageRequest request, Principal principal) {
        String senderEmail = principal.getName();
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        // Persist message to DB
        MessageResponse savedMessage = messageService.sendMessage(request.getReceiverId(), request.getContent());

        log.info("WebSocket chat: {} sent a message to {}", senderEmail, receiver.getEmail());

        // Send to receiver
        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/messages",
                savedMessage
        );

        // Send confirmation back to sender
        messagingTemplate.convertAndSendToUser(
                senderEmail,
                "/queue/messages",
                savedMessage
        );
    }
}
