package com.pms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// OOP — ENCAPSULATION: private fields with Lombok @Data — data carrier for API layer
public class CommentResponse {
    private Long id;
    private String message;
    private Long taskId;
    private UserResponse user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
