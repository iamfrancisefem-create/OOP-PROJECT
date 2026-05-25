package com.pms.service;

import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.ReportResponse;
import com.pms.entity.enums.ReportType;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    ReportResponse generateProjectReport(Long projectId, ReportType type);
    Resource downloadReport(Long reportId);
    PagedResponse<ReportResponse> getReportsByProject(Long projectId, Pageable pageable);
}
