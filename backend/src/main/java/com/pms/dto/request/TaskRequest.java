package com.pms.dto.request;

import com.pms.entity.enums.TaskPriority;
import com.pms.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    private String description;

    @NotNull(message = "Task status is required")
    private TaskStatus status;

    @NotNull(message = "Task priority is required")
    private TaskPriority priority;

    private LocalDate deadline;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assignedToId;
}
