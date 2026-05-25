package com.pms.dto.response;

import com.pms.entity.enums.MilestoneStatus;
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
public class MilestoneResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate deadline;
    private MilestoneStatus status;
    private Long projectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
