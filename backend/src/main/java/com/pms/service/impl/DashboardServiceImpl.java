package com.pms.service.impl;

import com.pms.dto.response.AnalyticsResponse;
import com.pms.dto.response.DashboardResponse;
import com.pms.entity.AuditLog;
import com.pms.entity.User;
import com.pms.entity.enums.TaskPriority;
import com.pms.entity.enums.TaskStatus;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.AuditLogRepository;
import com.pms.repository.ProjectRepository;
import com.pms.repository.TaskRepository;
import com.pms.repository.TeamMemberRepository;
import com.pms.repository.UserRepository;
import com.pms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
// OOP — INHERITANCE: implements DashboardService interface
// OOP — ENCAPSULATION: hides aggregation logic for dashboard stats
// OOP — COMPOSITION: composed of ProjectRepository, TaskRepository, UserRepository, AuditLogRepository, TeamMemberRepository
public class DashboardServiceImpl implements DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final TeamMemberRepository teamMemberRepository;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats() {
        User currentUser = getCurrentAuthenticatedUser();
        Long userId = currentUser.getId();

        long totalProjects = projectRepository.countByUserAccess(userId);
        long totalMembers = teamMemberRepository.countDistinctUsersByUserAccess(userId);
        long completedTasks = taskRepository.countByStatusAndUserAccess(TaskStatus.DONE, userId);
        
        long todoTasks = taskRepository.countByStatusAndUserAccess(TaskStatus.TODO, userId);
        long inProgressTasks = taskRepository.countByStatusAndUserAccess(TaskStatus.IN_PROGRESS, userId);
        long testingTasks = taskRepository.countByStatusAndUserAccess(TaskStatus.TESTING, userId);
        long pendingTasks = todoTasks + inProgressTasks + testingTasks;

        long overdueTasks = taskRepository.countOverdueTasksByUserAccess(userId, LocalDate.now());

        long totalTasks = completedTasks + pendingTasks;
        double productivity = totalTasks > 0 ? ((double) completedTasks / totalTasks) * 100.0 : 0.0;

        Map<String, Long> tasksByStatus = new HashMap<>();
        tasksByStatus.put("TODO", todoTasks);
        tasksByStatus.put("IN_PROGRESS", inProgressTasks);
        tasksByStatus.put("TESTING", testingTasks);
        tasksByStatus.put("DONE", completedTasks);

        Map<String, Long> tasksByPriority = new HashMap<>();
        for (TaskPriority priority : TaskPriority.values()) {
            long count = taskRepository.countByPriorityAndUserAccess(priority, userId);
            tasksByPriority.put(priority.name(), count);
        }

        return DashboardResponse.builder()
                .totalProjects(totalProjects)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .overdueTasks(overdueTasks)
                .totalMembers(totalMembers)
                .productivityPercentage(productivity)
                .tasksByStatus(tasksByStatus)
                .tasksByPriority(tasksByPriority)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics() {
        User currentUser = getCurrentAuthenticatedUser();
        Long userId = currentUser.getId();

        // 1. Fetch recent activities from Audit Logs
        Page<AuditLog> auditLogs = auditLogRepository.findAll(
                PageRequest.of(0, 10, Sort.by("createdAt").descending())
        );
        List<Map<String, Object>> recentActivities = auditLogs.getContent().stream()
                .map(log -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", log.getId());
                    activity.put("action", log.getAction());
                    activity.put("entityType", log.getEntityType());
                    activity.put("entityId", log.getEntityId());
                    activity.put("details", log.getDetails());
                    activity.put("ipAddress", log.getIpAddress());
                    activity.put("timestamp", log.getCreatedAt());
                    activity.put("userEmail", log.getUser() != null ? log.getUser().getEmail() : "SYSTEM");
                    return activity;
                })
                .toList();

        // 2. Team performance statistics (scoped to user's projects)
        List<Map<String, Object>> teamPerformance = new ArrayList<>();
        userRepository.findAll(PageRequest.of(0, 5)).getContent().forEach(user -> {
            long userTotal = taskRepository.countByAssignedToScoped(user, userId);
            long userDone = taskRepository.countByAssignedToAndStatusScoped(user, TaskStatus.DONE, userId);
            if (userTotal == 0 && userDone == 0) return;
            double score = userTotal > 0 ? ((double) userDone / userTotal) * 100.0 : 0.0;
            
            Map<String, Object> performance = new HashMap<>();
            performance.put("userId", user.getId());
            performance.put("userName", user.getFullName());
            performance.put("totalTasks", userTotal);
            performance.put("completedTasks", userDone);
            performance.put("efficiencyScore", score);
            teamPerformance.add(performance);
        });

        // 3. Workload distribution (scoped to user's projects)
        List<Map<String, Object>> workloadDistribution = new ArrayList<>();
        userRepository.findAll(PageRequest.of(0, 5)).getContent().forEach(user -> {
            long todo = taskRepository.countByAssignedToAndStatusScoped(user, TaskStatus.TODO, userId);
            long inProgress = taskRepository.countByAssignedToAndStatusScoped(user, TaskStatus.IN_PROGRESS, userId);
            long testing = taskRepository.countByAssignedToAndStatusScoped(user, TaskStatus.TESTING, userId);
            if (todo == 0 && inProgress == 0 && testing == 0) return;

            Map<String, Object> workload = new HashMap<>();
            workload.put("userName", user.getFullName());
            workload.put("todo", todo);
            workload.put("inProgress", inProgress);
            workload.put("testing", testing);
            workloadDistribution.add(workload);
        });

        return AnalyticsResponse.builder()
                .recentActivities(recentActivities)
                .teamPerformance(teamPerformance)
                .workloadDistribution(workloadDistribution)
                .build();
    }
}
