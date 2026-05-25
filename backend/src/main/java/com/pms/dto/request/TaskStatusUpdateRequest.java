package com.pms.dto.request;

import com.pms.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusUpdateRequest {

    @NotNull(message = "Task status is required")
    private TaskStatus status;
}
