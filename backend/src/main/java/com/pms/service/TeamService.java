package com.pms.service;

import com.pms.dto.request.TeamRequest;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.TeamResponse;
import org.springframework.data.domain.Pageable;

public interface TeamService {
    TeamResponse createTeam(TeamRequest request);
    PagedResponse<TeamResponse> getAllTeams(Pageable pageable);
    TeamResponse getTeamById(Long id);
    TeamResponse updateTeam(Long id, TeamRequest request);
    void deleteTeam(Long id);
    void addMember(Long teamId, Long userId, String role);
    void removeMember(Long teamId, Long userId);
}
