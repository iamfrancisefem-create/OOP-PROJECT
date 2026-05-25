package com.pms.service;

import com.pms.dto.request.TaskRequest;
import com.pms.dto.response.TaskResponse;
import com.pms.entity.Project;
import com.pms.entity.Task;
import com.pms.entity.User;
import com.pms.entity.enums.TaskPriority;
import com.pms.entity.enums.TaskStatus;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.TaskMapper;
import com.pms.repository.ProjectRepository;
import com.pms.repository.TaskRepository;
import com.pms.repository.UserRepository;
import com.pms.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private SecurityContext originalSecurityContext;

    @BeforeEach
    public void setup() {
        originalSecurityContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.setContext(originalSecurityContext);
    }

    @Test
    public void testCreateTask_Success() {
        // Arrange
        TaskRequest request = TaskRequest.builder()
                .title("New Task")
                .description("Task details")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .projectId(1L)
                .assignedToId(2L)
                .deadline(LocalDate.now().plusDays(5))
                .build();

        User creator = User.builder().email("admin@example.com").build();
        User assignee = User.builder().id(2L).email("dev@example.com").build();
        Project project = Project.builder().id(1L).title("Main Project").build();
        
        Task task = Task.builder().title("New Task").build();
        Task savedTask = Task.builder().id(100L).title("New Task").project(project).build();
        TaskResponse responseDto = TaskResponse.builder().id(100L).title("New Task").build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("admin@example.com");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(creator));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskMapper.toEntity(request)).thenReturn(task);
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        
        // Progress calculation mocks inside helper
        when(taskRepository.countByProject(project)).thenReturn(1L);
        when(taskRepository.countByProjectAndStatus(project, TaskStatus.DONE)).thenReturn(0L);
        when(projectRepository.save(project)).thenReturn(project);

        when(taskMapper.toResponse(savedTask)).thenReturn(responseDto);

        // Act
        TaskResponse result = taskService.createTask(request);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(taskRepository).save(task);
        verify(projectRepository).save(project);
    }

    @Test
    public void testGetTaskById_Success() {
        // Arrange
        Task task = Task.builder().id(20L).title("Some Task").build();
        TaskResponse response = TaskResponse.builder().id(20L).title("Some Task").build();

        when(taskRepository.findById(20L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        // Act
        TaskResponse result = taskService.getTaskById(20L);

        // Assert
        assertNotNull(result);
        assertEquals(20L, result.getId());
        assertEquals("Some Task", result.getTitle());
    }

    @Test
    public void testGetTaskById_NotFound_ShouldThrowException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(999L));
    }

    @Test
    public void testUpdateStatus_Success() {
        // Arrange
        Project project = Project.builder().id(1L).build();
        Task task = Task.builder().id(1L).project(project).status(TaskStatus.TODO).build();
        Task updatedTask = Task.builder().id(1L).project(project).status(TaskStatus.IN_PROGRESS).build();
        TaskResponse response = TaskResponse.builder().id(1L).status(TaskStatus.IN_PROGRESS).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(updatedTask);
        
        // Progress calculation mocks
        when(taskRepository.countByProject(project)).thenReturn(2L);
        when(taskRepository.countByProjectAndStatus(project, TaskStatus.DONE)).thenReturn(1L);
        when(projectRepository.save(project)).thenReturn(project);

        when(taskMapper.toResponse(updatedTask)).thenReturn(response);

        // Act
        TaskResponse result = taskService.updateStatus(1L, TaskStatus.IN_PROGRESS);

        // Assert
        assertNotNull(result);
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(taskRepository).save(task);
        verify(projectRepository).save(project);
    }
}
