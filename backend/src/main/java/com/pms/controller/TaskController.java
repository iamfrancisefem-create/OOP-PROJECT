package com.pms.controller;

import com.pms.dto.request.TaskRequest;
import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.TaskResponse;
import com.pms.entity.enums.TaskStatus;
import com.pms.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getAllTasks(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PagedResponse<TaskResponse> response = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully.", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request
    ) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully.", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully.", null));
    }

    // Supports both PATCH /tasks/{id}/status and PATCH /tasks/status per prompt spec
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status
    ) {
        TaskResponse response = taskService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Task status updated successfully.", response));
    }

    @PatchMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatusQuery(
            @RequestParam Long taskId,
            @RequestParam TaskStatus status
    ) {
        TaskResponse response = taskService.updateStatus(taskId, status);
        return ResponseEntity.ok(ApiResponse.success("Task status updated successfully.", response));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long id,
            @RequestParam(required = false) Long assignedToId
    ) {
        TaskResponse response = taskService.assignTask(id, assignedToId);
        return ResponseEntity.ok(ApiResponse.success("Task assigned successfully.", response));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getTasksByProject(
            @PathVariable Long projectId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PagedResponse<TaskResponse> response = taskService.getTasksByProjectId(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully for project.", response));
    }

    @GetMapping("/assigned/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getTasksByAssignedUser(
            @PathVariable Long userId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PagedResponse<TaskResponse> response = taskService.getTasksByAssignedUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully for assigned user.", response));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks() {
        List<TaskResponse> response = taskService.getOverdueTasks();
        return ResponseEntity.ok(ApiResponse.success("Overdue tasks retrieved successfully.", response));
    }
}
