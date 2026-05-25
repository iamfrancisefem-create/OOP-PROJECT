package com.pms.dto.response;

import com.pms.entity.enums.ProjectPriority;
import com.pms.entity.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private ProjectStatus status;
    private ProjectPriority priority;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double progress;
    private UserResponse createdBy;
    private TeamResponse team;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
