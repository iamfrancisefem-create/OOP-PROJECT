package com.pms.service;

import com.pms.dto.request.MilestoneRequest;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.MilestoneResponse;
import com.pms.entity.enums.MilestoneStatus;
import org.springframework.data.domain.Pageable;

public interface MilestoneService {
    MilestoneResponse createMilestone(MilestoneRequest request);
    PagedResponse<MilestoneResponse> getAllMilestones(Pageable pageable);
    MilestoneResponse getMilestoneById(Long id);
    MilestoneResponse updateMilestone(Long id, MilestoneRequest request);
    void deleteMilestone(Long id);
    MilestoneResponse updateStatus(Long id, MilestoneStatus status);
    PagedResponse<MilestoneResponse> getMilestonesByProjectId(Long projectId, Pageable pageable);
}
