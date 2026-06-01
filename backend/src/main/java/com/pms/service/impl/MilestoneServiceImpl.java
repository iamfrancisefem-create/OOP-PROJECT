package com.pms.service.impl;

import com.pms.dto.request.MilestoneRequest;
import com.pms.dto.response.MilestoneResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.entity.Milestone;
import com.pms.entity.Project;
import com.pms.entity.User;
import com.pms.entity.enums.MilestoneStatus;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.MilestoneMapper;
import com.pms.repository.MilestoneRepository;
import com.pms.repository.ProjectRepository;
import com.pms.repository.UserRepository;
import com.pms.service.MilestoneService;
import com.pms.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
// OOP — INHERITANCE: implements MilestoneService interface
// OOP — ENCAPSULATION: hides milestone CRUD and status update logic
// OOP — COMPOSITION: composed of MilestoneRepository, ProjectRepository, MilestoneMapper, UserRepository, TeamMemberRepository
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneMapper milestoneMapper;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private void validateProjectAccess(Project project) {
        User currentUser = getCurrentAuthenticatedUser();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ADMIN"));
        boolean isCreator = project.getCreatedBy() != null && project.getCreatedBy().getId().equals(currentUser.getId());
        boolean isTeamMember = project.getTeam() != null && teamMemberRepository.existsByTeamAndUser(project.getTeam(), currentUser);

        if (!isAdmin && !isCreator && !isTeamMember) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not have permission to access milestones for this project.");
        }
    }

    @Override
    @Transactional
    public MilestoneResponse createMilestone(MilestoneRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));

        validateProjectAccess(project);

        Milestone milestone = milestoneMapper.toEntity(request);
        milestone.setProject(project);

        Milestone savedMilestone = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(savedMilestone);
    }

    @Override
    public PagedResponse<MilestoneResponse> getAllMilestones(Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();
        Page<Milestone> milestonesPage = milestoneRepository.findByUserAccess(currentUser.getId(), pageable);
        List<MilestoneResponse> content = milestonesPage.getContent().stream()
                .map(milestoneMapper::toResponse)
                .toList();

        return PagedResponse.<MilestoneResponse>builder()
                .content(content)
                .page(milestonesPage.getNumber())
                .size(milestonesPage.getSize())
                .totalElements(milestonesPage.getTotalElements())
                .totalPages(milestonesPage.getTotalPages())
                .last(milestonesPage.isLast())
                .build();
    }

    @Override
    public MilestoneResponse getMilestoneById(Long id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + id));
        validateProjectAccess(milestone.getProject());
        return milestoneMapper.toResponse(milestone);
    }

    @Override
    @Transactional
    public MilestoneResponse updateMilestone(Long id, MilestoneRequest request) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + id));

        validateProjectAccess(milestone.getProject());

        milestone.setTitle(request.getTitle());
        milestone.setDescription(request.getDescription());
        milestone.setDeadline(request.getDeadline());
        milestone.setStatus(request.getStatus());

        if (!milestone.getProject().getId().equals(request.getProjectId())) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));
            validateProjectAccess(project);
            milestone.setProject(project);
        }

        Milestone updatedMilestone = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(updatedMilestone);
    }

    @Override
    @Transactional
    public void deleteMilestone(Long id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + id));

        validateProjectAccess(milestone.getProject());

        milestoneRepository.delete(milestone);
    }

    @Override
    @Transactional
    public MilestoneResponse updateStatus(Long id, MilestoneStatus status) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + id));

        validateProjectAccess(milestone.getProject());

        milestone.setStatus(status);
        Milestone updatedMilestone = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(updatedMilestone);
    }

    @Override
    public PagedResponse<MilestoneResponse> getMilestonesByProjectId(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        validateProjectAccess(project);

        Page<Milestone> milestonesPage = milestoneRepository.findByProject(project, pageable);
        List<MilestoneResponse> content = milestonesPage.getContent().stream()
                .map(milestoneMapper::toResponse)
                .toList();
        return PagedResponse.<MilestoneResponse>builder()
                .content(content)
                .page(milestonesPage.getNumber())
                .size(milestonesPage.getSize())
                .totalElements(milestonesPage.getTotalElements())
                .totalPages(milestonesPage.getTotalPages())
                .last(milestonesPage.isLast())
                .build();
    }
}
