package com.pms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// OOP — ENCAPSULATION: private fields with Lombok @Data — data carrier for API layer
public class TeamResponse {
    private Long id;
    private String name;
    private String description;
    private UserResponse createdBy;
    private List<TeamMemberResponse> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
