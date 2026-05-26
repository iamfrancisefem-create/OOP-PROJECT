package com.pms.service.impl;

import com.pms.dto.request.MilestoneRequest;
import com.pms.dto.response.MilestoneResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.entity.Milestone;
import com.pms.entity.Project;
import com.pms.entity.enums.MilestoneStatus;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.MilestoneMapper;
import com.pms.repository.MilestoneRepository;
import com.pms.repository.ProjectRepository;
import com.pms.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneMapper milestoneMapper;

    @Override
    @Transactional
    public MilestoneResponse createMilestone(MilestoneRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));

        Milestone milestone = milestoneMapper.toEntity(request);
        milestone.setProject(project);

        Milestone savedMilestone = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(savedMilestone);
    }

    @Override
    public PagedResponse<MilestoneResponse> getAllMilestones(Pageable pageable) {
        Page<Milestone> milestonesPage = milestoneRepository.findAll(pageable);
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
        return milestoneMapper.toResponse(milestone);
    }

    @Override
    @Transactional
    public MilestoneResponse updateMilestone(Long id, MilestoneRequest request) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + id));

        milestone.setTitle(request.getTitle());
        milestone.setDescription(request.getDescription());
        milestone.setDeadline(request.getDeadline());
        milestone.setStatus(request.getStatus());

        if (!milestone.getProject().getId().equals(request.getProjectId())) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));
            milestone.setProject(project);
        }

        Milestone updatedMilestone = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(updatedMilestone);
    }

    @Override
    @Transactional
    public void deleteMilestone(Long id) {
        if (!milestoneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Milestone not found with ID: " + id);
        }
        milestoneRepository.deleteById(id);
    }

    @Override
    @Transactional
    public MilestoneResponse updateStatus(Long id, MilestoneStatus status) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + id));
        milestone.setStatus(status);
        Milestone updatedMilestone = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(updatedMilestone);
    }

    @Override
    public PagedResponse<MilestoneResponse> getMilestonesByProjectId(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
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
