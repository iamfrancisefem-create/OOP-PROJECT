package com.pms.service.impl;

import com.pms.dto.request.TeamRequest;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.TeamMemberResponse;
import com.pms.dto.response.TeamResponse;
import com.pms.entity.Team;
import com.pms.entity.TeamMember;
import com.pms.entity.User;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.TeamMapper;
import com.pms.repository.TeamMemberRepository;
import com.pms.repository.TeamRepository;
import com.pms.repository.UserRepository;
import com.pms.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        User creator = getCurrentAuthenticatedUser();
        Team team = teamMapper.toEntity(request);
        team.setCreatedBy(creator);
        
        Team savedTeam = teamRepository.save(team);
        
        // Automatically add the creator as the "LEADER" of the team
        TeamMember leader = TeamMember.builder()
                .team(savedTeam)
                .user(creator)
                .role("LEADER")
                .joinedAt(LocalDateTime.now())
                .build();
        teamMemberRepository.save(leader);

        TeamResponse response = teamMapper.toResponse(savedTeam);
        response.setMembers(buildMemberResponses(List.of(leader)));
        return response;
    }

    @Override
    public PagedResponse<TeamResponse> getAllTeams(Pageable pageable) {
        Page<Team> teamsPage = teamRepository.findAll(pageable);
        List<TeamResponse> content = teamsPage.getContent().stream()
                .map(team -> {
                    TeamResponse resp = teamMapper.toResponse(team);
                    resp.setMembers(Collections.emptyList());
                    return resp;
                })
                .toList();

        return PagedResponse.<TeamResponse>builder()
                .content(content)
                .page(teamsPage.getNumber())
                .size(teamsPage.getSize())
                .totalElements(teamsPage.getTotalElements())
                .totalPages(teamsPage.getTotalPages())
                .last(teamsPage.isLast())
                .build();
    }

    @Override
    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + id));
        TeamResponse response = teamMapper.toResponse(team);
        List<TeamMember> members = teamMemberRepository.findByTeam(team);
        response.setMembers(buildMemberResponses(members));
        return response;
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(Long id, TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + id));

        team.setName(request.getName());
        team.setDescription(request.getDescription());

        Team updatedTeam = teamRepository.save(team);
        return teamMapper.toResponse(updatedTeam);
    }

    @Override
    @Transactional
    public void deleteTeam(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team not found with ID: " + id);
        }
        teamRepository.deleteById(id);
    }

    private List<TeamMemberResponse> buildMemberResponses(List<TeamMember> members) {
        return members.stream().map(m -> TeamMemberResponse.builder()
                .id(m.getId())
                .userId(m.getUser().getId())
                .fullName(m.getUser().getFullName())
                .email(m.getUser().getEmail())
                .role(m.getRole())
                .joinedAt(m.getJoinedAt())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addMember(Long teamId, Long userId, String role) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + teamId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (teamMemberRepository.findByTeamAndUser(team, user).isPresent()) {
            throw new BadRequestException("User is already a member of this team");
        }

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(role != null ? role : "MEMBER")
                .joinedAt(LocalDateTime.now())
                .build();

        teamMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void removeMember(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + teamId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        TeamMember member = teamMemberRepository.findByTeamAndUser(team, user)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found for this user in this team"));

        teamMemberRepository.delete(member);
    }
}
