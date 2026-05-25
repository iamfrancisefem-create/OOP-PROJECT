package com.pms.service.impl;

import com.pms.dto.request.TaskRequest;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.TaskResponse;
import com.pms.entity.Project;
import com.pms.entity.Task;
import com.pms.entity.User;
import com.pms.entity.enums.TaskStatus;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.TaskMapper;
import com.pms.repository.ProjectRepository;
import com.pms.repository.TaskRepository;
import com.pms.repository.UserRepository;
import com.pms.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private void updateProjectProgress(Project project) {
        long totalTasks = taskRepository.countByProject(project);
        if (totalTasks == 0) {
            project.setProgress(0.0);
        } else {
            long completedTasks = taskRepository.countByProjectAndStatus(project, TaskStatus.DONE);
            double progress = ((double) completedTasks / totalTasks) * 100.0;
            project.setProgress(progress);
        }
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        User creator = getCurrentAuthenticatedUser();
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));

        Task task = taskMapper.toEntity(request);
        task.setProject(project);
        task.setCreatedBy(creator);

        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getAssignedToId()));
            task.setAssignedTo(assignedTo);
        }

        Task savedTask = taskRepository.save(task);
        updateProjectProgress(project);

        return taskMapper.toResponse(savedTask);
    }

    @Override
    public PagedResponse<TaskResponse> getAllTasks(Pageable pageable) {
        Page<Task> tasksPage = taskRepository.findAll(pageable);
        List<TaskResponse> content = tasksPage.getContent().stream()
                .map(taskMapper::toResponse)
                .toList();

        return PagedResponse.<TaskResponse>builder()
                .content(content)
                .page(tasksPage.getNumber())
                .size(tasksPage.getSize())
                .totalElements(tasksPage.getTotalElements())
                .totalPages(tasksPage.getTotalPages())
                .last(tasksPage.isLast())
                .build();
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        
        Project oldProject = task.getProject();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDeadline(request.getDeadline());

        if (!task.getProject().getId().equals(request.getProjectId())) {
            Project newProject = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));
            task.setProject(newProject);
        }

        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getAssignedToId()));
            task.setAssignedTo(assignedTo);
        } else {
            task.setAssignedTo(null);
        }

        Task updatedTask = taskRepository.save(task);
        
        updateProjectProgress(oldProject);
        if (!oldProject.getId().equals(updatedTask.getProject().getId())) {
            updateProjectProgress(updatedTask.getProject());
        }

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        Project project = task.getProject();
        taskRepository.delete(task);
        updateProjectProgress(project);
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        updateProjectProgress(task.getProject());
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse assignTask(Long id, Long assignedToId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        if (assignedToId != null) {
            User assignedTo = userRepository.findById(assignedToId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + assignedToId));
            task.setAssignedTo(assignedTo);
        } else {
            task.setAssignedTo(null);
        }

        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public PagedResponse<TaskResponse> getTasksByProjectId(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        Page<Task> tasksPage = taskRepository.findByProject(project, pageable);
        List<TaskResponse> content = tasksPage.getContent().stream()
                .map(taskMapper::toResponse)
                .toList();

        return PagedResponse.<TaskResponse>builder()
                .content(content)
                .page(tasksPage.getNumber())
                .size(tasksPage.getSize())
                .totalElements(tasksPage.getTotalElements())
                .totalPages(tasksPage.getTotalPages())
                .last(tasksPage.isLast())
                .build();
    }

    @Override
    public PagedResponse<TaskResponse> getTasksByAssignedUserId(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Page<Task> tasksPage = taskRepository.findByAssignedTo(user, pageable);
        List<TaskResponse> content = tasksPage.getContent().stream()
                .map(taskMapper::toResponse)
                .toList();

        return PagedResponse.<TaskResponse>builder()
                .content(content)
                .page(tasksPage.getNumber())
                .size(tasksPage.getSize())
                .totalElements(tasksPage.getTotalElements())
                .totalPages(tasksPage.getTotalPages())
                .last(tasksPage.isLast())
                .build();
    }

    @Override
    public List<TaskResponse> getOverdueTasks() {
        List<Task> overdueTasks = taskRepository.findOverdueTasks(LocalDate.now());
        return overdueTasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }
}
