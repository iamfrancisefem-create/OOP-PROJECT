package com.pms.dto.response;

import com.pms.entity.enums.TaskPriority;
import com.pms.entity.enums.TaskStatus;
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
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate deadline;
    private Long projectId;
    private UserResponse assignedTo;
    private UserResponse createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
