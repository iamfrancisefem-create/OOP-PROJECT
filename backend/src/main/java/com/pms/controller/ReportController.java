package com.pms.controller;

import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.ReportResponse;
import com.pms.entity.Report;
import com.pms.entity.enums.ReportType;
import com.pms.repository.ReportRepository;
import com.pms.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportRepository reportRepository;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ReportResponse>> generateReport(
            @RequestParam Long projectId,
            @RequestParam ReportType reportType
    ) {
        ReportResponse response = reportService.generateProjectReport(projectId, reportType);
        return ResponseEntity.ok(ApiResponse.success("Report successfully generated and archived.", response));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long id) {
        Resource resource = reportService.downloadReport(id);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report details not found in database"));

        String contentType = "application/octet-stream";
        String fileName = "Report_" + id;
        if (report.getFileUrl() != null && report.getFileUrl().endsWith(".pdf")) {
            contentType = "application/pdf";
            fileName += ".pdf";
        } else {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            fileName += ".xlsx";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<PagedResponse<ReportResponse>>> getReportsByProject(
            @PathVariable Long projectId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PagedResponse<ReportResponse> response = reportService.getReportsByProject(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Project reports history retrieved successfully.", response));
    }
}
