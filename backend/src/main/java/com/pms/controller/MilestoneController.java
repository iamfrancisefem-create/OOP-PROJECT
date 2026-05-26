package com.pms.controller;

import com.pms.dto.request.MilestoneRequest;
import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.MilestoneResponse;
import com.pms.entity.enums.MilestoneStatus;
import com.pms.service.MilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/milestones")
@RequiredArgsConstructor
public class MilestoneController {

    private final MilestoneService milestoneService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<MilestoneResponse>> createMilestone(@Valid @RequestBody MilestoneRequest request) {
        MilestoneResponse response = milestoneService.createMilestone(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Milestone created successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<MilestoneResponse>>> getAllMilestones(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PagedResponse<MilestoneResponse> response = milestoneService.getAllMilestones(pageable);
        return ResponseEntity.ok(ApiResponse.success("Milestones retrieved successfully.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MilestoneResponse>> getMilestoneById(@PathVariable Long id) {
        MilestoneResponse response = milestoneService.getMilestoneById(id);
        return ResponseEntity.ok(ApiResponse.success("Milestone retrieved successfully.", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<MilestoneResponse>> updateMilestone(
            @PathVariable Long id,
            @Valid @RequestBody MilestoneRequest request
    ) {
        MilestoneResponse response = milestoneService.updateMilestone(id, request);
        return ResponseEntity.ok(ApiResponse.success("Milestone updated successfully.", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteMilestone(@PathVariable Long id) {
        milestoneService.deleteMilestone(id);
        return ResponseEntity.ok(ApiResponse.success("Milestone deleted successfully.", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<MilestoneResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam MilestoneStatus status
    ) {
        MilestoneResponse response = milestoneService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Milestone status updated successfully.", response));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<PagedResponse<MilestoneResponse>>> getMilestonesByProject(
            @PathVariable Long projectId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PagedResponse<MilestoneResponse> response = milestoneService.getMilestonesByProjectId(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Milestones retrieved successfully for project.", response));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<MilestoneResponse>> completeMilestone(@PathVariable Long id) {
        MilestoneResponse response = milestoneService.updateStatus(id, MilestoneStatus.COMPLETED);
        return ResponseEntity.ok(ApiResponse.success("Milestone marked as completed.", response));
    }
}
