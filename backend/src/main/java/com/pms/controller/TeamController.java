package com.pms.controller;

import com.pms.dto.request.TeamRequest;
import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.TeamResponse;
import com.pms.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@Valid @RequestBody TeamRequest request) {
        TeamResponse response = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Team created successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TeamResponse>>> getAllTeams(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PagedResponse<TeamResponse> response = teamService.getAllTeams(pageable);
        return ResponseEntity.ok(ApiResponse.success("Teams retrieved successfully.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(@PathVariable Long id) {
        TeamResponse response = teamService.getTeamById(id);
        return ResponseEntity.ok(ApiResponse.success("Team retrieved successfully.", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody TeamRequest request
    ) {
        TeamResponse response = teamService.updateTeam(id, request);
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully.", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully.", null));
    }

    @PostMapping("/{teamId}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable Long teamId,
            @RequestParam Long userId,
            @RequestParam(required = false) String role
    ) {
        teamService.addMember(teamId, userId, role);
        return ResponseEntity.ok(ApiResponse.success("Member added to team successfully.", null));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long userId
    ) {
        teamService.removeMember(teamId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed from team successfully.", null));
    }
}
