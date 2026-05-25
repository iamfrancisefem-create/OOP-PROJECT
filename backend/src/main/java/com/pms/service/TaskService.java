package com.pms.service;

import com.pms.dto.request.TaskRequest;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.TaskResponse;
import com.pms.entity.enums.TaskStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(TaskRequest request);
    PagedResponse<TaskResponse> getAllTasks(Pageable pageable);
    TaskResponse getTaskById(Long id);
    TaskResponse updateTask(Long id, TaskRequest request);
    void deleteTask(Long id);
    TaskResponse updateStatus(Long id, TaskStatus status);
    TaskResponse assignTask(Long id, Long assignedToId);
    PagedResponse<TaskResponse> getTasksByProjectId(Long projectId, Pageable pageable);
    PagedResponse<TaskResponse> getTasksByAssignedUserId(Long userId, Pageable pageable);
    List<TaskResponse> getOverdueTasks();
}
