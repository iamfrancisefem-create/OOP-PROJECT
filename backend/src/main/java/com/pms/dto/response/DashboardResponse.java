package com.pms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// OOP — ENCAPSULATION: private fields with Lombok @Data — data carrier for API layer
public class DashboardResponse {
    private Long totalProjects;
    private Long completedTasks;
    private Long pendingTasks;
    private Long overdueTasks;
    private Long totalMembers;
    private Double productivityPercentage;
    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByPriority;
}
