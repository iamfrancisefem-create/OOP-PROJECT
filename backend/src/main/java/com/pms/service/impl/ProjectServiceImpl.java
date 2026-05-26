package com.pms.service.impl;

import com.pms.dto.request.ProjectRequest;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.ProjectResponse;
import com.pms.entity.Project;
import com.pms.entity.Team;
import com.pms.entity.User;
import com.pms.entity.enums.ProjectStatus;
import com.pms.entity.enums.TaskStatus;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.ProjectMapper;
import com.pms.repository.ProjectRepository;
import com.pms.repository.TaskRepository;
import com.pms.repository.TeamRepository;
import com.pms.repository.UserRepository;
import com.pms.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.pms.repository.TeamMemberRepository;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectMapper projectMapper;
    private final TeamMemberRepository teamMemberRepository;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User creator = getCurrentAuthenticatedUser();
        Project project = projectMapper.toEntity(request);
        project.setCreatedBy(creator);

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + request.getTeamId()));
            project.setTeam(team);
        }

        project.setProgress(0.0);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    @Override
    public PagedResponse<ProjectResponse> getAllProjects(Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();
        Page<Project> projectsPage = projectRepository.findAll(pageable);
        List<ProjectResponse> content = projectsPage.getContent().stream()
                .filter(project -> {
                    // creator
                    if (project.getCreatedBy() != null && project.getCreatedBy().getId().equals(currentUser.getId())) {
                        return true;
                    }
                    // team member
                    if (project.getTeam() != null) {
                        return teamMemberRepository.existsByTeamAndUser(project.getTeam(), currentUser);
                    }
                    return false;
                })
                .map(projectMapper::toResponse)
                .toList();

        return PagedResponse.<ProjectResponse>builder()
                .content(content)
                .page(projectsPage.getNumber())
                .size(projectsPage.getSize())
                .totalElements(projectsPage.getTotalElements())
                .totalPages(projectsPage.getTotalPages())
                .last(projectsPage.isLast())
                .build();
    }

    @Override
    public ProjectResponse getProjectById(Long id) {
        User currentUser = getCurrentAuthenticatedUser();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));
        boolean authorized = false;
        if (project.getCreatedBy() != null && project.getCreatedBy().getId().equals(currentUser.getId())) {
            authorized = true;
        } else if (project.getTeam() != null) {
            authorized = teamMemberRepository.existsByTeamAndUser(project.getTeam(), currentUser);
        }
        if (!authorized) {
            throw new com.pms.exception.BadRequestException("Access denied to project");
        }
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        project.setPriority(request.getPriority());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + request.getTeamId()));
            project.setTeam(team);
        } else {
            project.setTeam(null);
        }

        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found with ID: " + id);
        }
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ProjectResponse updateStatus(Long id, ProjectStatus status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));
        project.setStatus(status);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public Double calculateProgress(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        long totalTasks = taskRepository.countByProject(project);
        if (totalTasks == 0) {
            project.setProgress(0.0);
            projectRepository.save(project);
            return 0.0;
        }

        long completedTasks = taskRepository.countByProjectAndStatus(project, TaskStatus.DONE);
        double progress = ((double) completedTasks / totalTasks) * 100.0;
        project.setProgress(progress);
        projectRepository.save(project);

        return progress;
    }
}
