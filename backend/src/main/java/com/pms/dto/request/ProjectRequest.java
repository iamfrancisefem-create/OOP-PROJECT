package com.pms.dto.request;

import com.pms.entity.enums.ProjectPriority;
import com.pms.entity.enums.ProjectStatus;
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
public class ProjectRequest {

    @NotBlank(message = "Project title is required")
    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    private String description;

    @NotNull(message = "Project status is required")
    private ProjectStatus status;

    @NotNull(message = "Project priority is required")
    private ProjectPriority priority;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private Long teamId;
}
