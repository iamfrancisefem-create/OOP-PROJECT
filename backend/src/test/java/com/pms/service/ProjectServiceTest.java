package com.pms.service;

import com.pms.dto.request.ProjectRequest;
import com.pms.dto.response.ProjectResponse;
import com.pms.entity.Project;
import com.pms.entity.Team;
import com.pms.entity.User;
import com.pms.entity.enums.ProjectPriority;
import com.pms.entity.enums.ProjectStatus;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.ProjectMapper;
import com.pms.repository.ProjectRepository;
import com.pms.repository.TaskRepository;
import com.pms.repository.TeamRepository;
import com.pms.repository.UserRepository;
import com.pms.service.impl.ProjectServiceImpl;
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
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

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
    public void testCreateProject_Success() {
        // Arrange
        ProjectRequest request = ProjectRequest.builder()
                .title("New Project")
                .description("Project Description")
                .status(ProjectStatus.NEW)
                .priority(ProjectPriority.HIGH)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .teamId(1L)
                .build();

        User creator = User.builder().email("admin@example.com").build();
        Team team = Team.builder().id(1L).name("Dev Team").build();
        Project project = Project.builder().title("New Project").build();
        Project savedProject = Project.builder().id(10L).title("New Project").progress(0.0).build();
        ProjectResponse responseDto = ProjectResponse.builder().id(10L).title("New Project").progress(0.0).build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("admin@example.com");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(creator));
        when(projectMapper.toEntity(request)).thenReturn(project);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        when(projectMapper.toResponse(savedProject)).thenReturn(responseDto);

        // Act
        ProjectResponse result = projectService.createProject(request);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(0.0, result.getProgress());
        verify(projectRepository).save(project);
    }

    @Test
    public void testGetProjectById_Success() {
        // Arrange
        User currentUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .roles(java.util.Set.of(com.pms.entity.Role.builder().name(com.pms.entity.enums.RoleName.ADMIN).build()))
                .build();
        Project project = Project.builder().id(5L).title("Find Me").build();
        ProjectResponse response = ProjectResponse.builder().id(5L).title("Find Me").build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("admin@example.com");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(currentUser));
        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        when(projectMapper.toResponse(project)).thenReturn(response);

        // Act
        ProjectResponse result = projectService.getProjectById(5L);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Find Me", result.getTitle());
    }

    @Test
    public void testGetProjectById_NotFound_ShouldThrowException() {
        // Arrange
        User currentUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .roles(java.util.Set.of(com.pms.entity.Role.builder().name(com.pms.entity.enums.RoleName.ADMIN).build()))
                .build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("admin@example.com");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(currentUser));
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(99L));
    }

    @Test
    public void testUpdateStatus_Success() {
        // Arrange
        Project project = Project.builder().id(1L).title("Project").status(ProjectStatus.NEW).build();
        Project updatedProject = Project.builder().id(1L).title("Project").status(ProjectStatus.ACTIVE).build();
        ProjectResponse response = ProjectResponse.builder().id(1L).title("Project").status(ProjectStatus.ACTIVE).build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(updatedProject);
        when(projectMapper.toResponse(updatedProject)).thenReturn(response);

        // Act
        ProjectResponse result = projectService.updateStatus(1L, ProjectStatus.ACTIVE);

        // Assert
        assertNotNull(result);
        assertEquals(ProjectStatus.ACTIVE, result.getStatus());
        verify(projectRepository).save(project);
    }
}
