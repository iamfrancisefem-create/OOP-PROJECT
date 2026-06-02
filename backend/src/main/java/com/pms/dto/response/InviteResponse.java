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
public class InviteResponse {
    private Long id;
    private String email;
    private String role;
    private String token;
    private Boolean accepted;
    private String invitedBy;
    private LocalDateTime expiresAt;
    private String inviteLink;
    private LocalDateTime createdAt;
}
