package com.pms.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.ReportResponse;
import com.pms.entity.Project;
import com.pms.entity.Report;
import com.pms.entity.Task;
import com.pms.entity.User;
import com.pms.entity.enums.ReportType;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.ReportMapper;
import com.pms.repository.ProjectRepository;
import com.pms.repository.ReportRepository;
import com.pms.repository.TaskRepository;
import com.pms.repository.UserRepository;
import com.pms.service.ReportService;
import com.pms.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ReportMapper reportMapper;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional
    public ReportResponse generateProjectReport(Long projectId, ReportType type) {
        User user = getCurrentAuthenticatedUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        byte[] reportContent;
        String fileName;
        String contentType;

        if (type == ReportType.PROJECT_PROGRESS) {
            // PDF Generation
            reportContent = generatePdfReport(project);
            fileName = "Project_Progress_Report_" + projectId + ".pdf";
            contentType = "application/pdf";
        } else {
            // Excel Generation
            reportContent = generateExcelReport(project);
            fileName = "Task_Completion_Report_" + projectId + ".xlsx";
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }

        // Wrap as multipart file and save to disk via FileStorageService
        MultipartFile multipartFile = new ByteArrayMultipartFile(reportContent, fileName, contentType);
        String savedFileName = fileStorageService.storeFile(multipartFile);

        Report report = Report.builder()
                .reportType(type)
                .fileUrl(savedFileName)
                .parameters("{\"projectId\":" + projectId + ",\"title\":\"" + project.getTitle() + "\"}")
                .project(project)
                .generatedBy(user)
                .build();

        Report savedReport = reportRepository.save(report);
        log.info("Report of type {} successfully generated for project: {}", type, project.getTitle());

        return reportMapper.toResponse(savedReport);
    }

    private byte[] generatePdfReport(Project project) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            
            document.add(new Paragraph("PROJECT PROGRESS REPORT"));
            document.add(new Paragraph("========================================="));
            document.add(new Paragraph("Project Title: " + project.getTitle()));
            document.add(new Paragraph("Description: " + project.getDescription()));
            document.add(new Paragraph("Status: " + project.getStatus().name()));
            document.add(new Paragraph("Priority: " + project.getPriority().name()));
            document.add(new Paragraph("Start Date: " + project.getStartDate()));
            document.add(new Paragraph("End Date: " + project.getEndDate()));
            document.add(new Paragraph("Progress: " + String.format("%.2f", project.getProgress()) + "%"));
            
            document.add(new Paragraph("\nAssociated Tasks:"));
            List<Task> tasks = taskRepository.findAll().stream()
                    .filter(t -> t.getProject().getId().equals(project.getId()))
                    .toList();
            
            for (Task t : tasks) {
                document.add(new Paragraph("- " + t.getTitle() + " [" + t.getStatus() + "]"));
            }
            
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF project report", e);
        }
    }

    private byte[] generateExcelReport(Project project) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Task Completion Status");
            
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("TASK COMPLETION REPORT - " + project.getTitle().toUpperCase());
            
            Row header = sheet.createRow(2);
            header.createCell(0).setCellValue("Task ID");
            header.createCell(1).setCellValue("Title");
            header.createCell(2).setCellValue("Status");
            header.createCell(3).setCellValue("Priority");
            header.createCell(4).setCellValue("Deadline");
            
            List<Task> tasks = taskRepository.findAll().stream()
                    .filter(t -> t.getProject().getId().equals(project.getId()))
                    .toList();

            int rowIdx = 3;
            for (Task t : tasks) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getId());
                row.createCell(1).setCellValue(t.getTitle());
                row.createCell(2).setCellValue(t.getStatus().name());
                row.createCell(3).setCellValue(t.getPriority().name());
                row.createCell(4).setCellValue(t.getDeadline() != null ? t.getDeadline().toString() : "N/A");
            }
            
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel task completion report", e);
        }
    }

    @Override
    public Resource downloadReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + reportId));
        return fileStorageService.loadFileAsResource(report.getFileUrl());
    }

    @Override
    public PagedResponse<ReportResponse> getReportsByProject(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        Page<Report> page = reportRepository.findByProject(project, pageable);
        List<ReportResponse> content = page.getContent().stream()
                .map(reportMapper::toResponse)
                .toList();

        return PagedResponse.<ReportResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // Custom implementation of MultipartFile to wrap in-memory report bytes
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String name;
        private final String contentType;

        public ByteArrayMultipartFile(byte[] content, String name, String contentType) {
            this.content = content;
            this.name = name;
            this.contentType = contentType;
        }

        @Override
        public String getName() { return name; }
        @Override
        public String getOriginalFilename() { return name; }
        @Override
        public String getContentType() { return contentType; }
        @Override
        public boolean isEmpty() { return content == null || content.length == 0; }
        @Override
        public long getSize() { return content.length; }
        @Override
        public byte[] getBytes() { return content; }
        @Override
        public InputStream getInputStream() { return new ByteArrayInputStream(content); }
        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            throw new UnsupportedOperationException("Transferring report file to custom destination not supported");
        }
    }
}
