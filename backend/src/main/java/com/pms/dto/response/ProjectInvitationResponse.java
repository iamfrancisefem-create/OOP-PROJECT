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
public class ProjectInvitationResponse {
    private Long id;
    private ProjectResponse project;
    private UserResponse invitedUser;
    private String token;
    private LocalDateTime expiresAt;
    private Boolean accepted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
