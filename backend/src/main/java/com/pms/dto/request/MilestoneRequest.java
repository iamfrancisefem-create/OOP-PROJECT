package com.pms.dto.request;

import com.pms.entity.enums.MilestoneStatus;
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
public class MilestoneRequest {

    @NotBlank(message = "Milestone title is required")
    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    private String description;

    @NotNull(message = "Milestone deadline is required")
    private LocalDate deadline;

    @NotNull(message = "Milestone status is required")
    private MilestoneStatus status;

    @NotNull(message = "Project ID is required")
    private Long projectId;
}
