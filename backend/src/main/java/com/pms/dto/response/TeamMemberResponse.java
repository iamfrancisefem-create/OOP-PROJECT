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
public class TeamMemberResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime joinedAt;
}
