package com.pms.service;

import com.pms.dto.request.ProjectRequest;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.ProjectResponse;
import com.pms.entity.enums.ProjectStatus;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request);
    PagedResponse<ProjectResponse> getAllProjects(Pageable pageable);
    ProjectResponse getProjectById(Long id);
    ProjectResponse updateProject(Long id, ProjectRequest request);
    void deleteProject(Long id);
    ProjectResponse updateStatus(Long id, ProjectStatus status);
    Double calculateProgress(Long id);
}
