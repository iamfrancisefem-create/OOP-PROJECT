package com.pms.controller;

import com.pms.dto.request.ProjectRequest;
import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.ProjectResponse;
import com.pms.entity.enums.ProjectStatus;
import com.pms.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProjectResponse>>> getAllProjects(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PagedResponse<ProjectResponse> response = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(ApiResponse.success("Projects retrieved successfully.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long id) {
        ProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success("Project retrieved successfully.", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request
    ) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully.", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully.", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam ProjectStatus status
    ) {
        ProjectResponse response = projectService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Project status updated successfully.", response));
    }

    @PostMapping("/{id}/calculate-progress")
    public ResponseEntity<ApiResponse<Double>> calculateProgress(@PathVariable Long id) {
        Double progress = projectService.calculateProgress(id);
        return ResponseEntity.ok(ApiResponse.success("Project progress calculated successfully.", progress));
    }
}
