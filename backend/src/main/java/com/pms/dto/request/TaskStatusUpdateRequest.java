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
// OOP — ENCAPSULATION: private fields with Lombok @Data — data carrier for API layer
public class TaskStatusUpdateRequest {

    @NotNull(message = "Task status is required")
    private TaskStatus status;
}
