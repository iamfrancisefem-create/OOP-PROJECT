package com.pms.scheduler;

import com.pms.entity.Project;
import com.pms.entity.enums.ReportType;
import com.pms.repository.ProjectRepository;
import com.pms.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportScheduler {

    private final ProjectRepository projectRepository;
    private final ReportService reportService;

    /**
     * Periodically triggers weekly summary exports for all active projects.
     * Scheduled every Sunday midnight: "0 0 0 * * SUN"
     */
    @Scheduled(cron = "0 0 0 * * SUN")
    public void generateWeeklySummaries() {
        log.info("Report Scheduler started weekly automated report generation...");
        List<Project> activeProjects = projectRepository.findAll();
        
        int count = 0;
        for (Project project : activeProjects) {
            try {
                // Automate weekly progress report creation
                reportService.generateProjectReport(project.getId(), ReportType.PROJECT_PROGRESS);
                count++;
            } catch (Exception e) {
                log.error("Failed to generate automated weekly report for project {}: {}", 
                        project.getTitle(), e.getMessage());
            }
        }
        
        log.info("Report Scheduler finished. Weekly summaries generated for {} projects.", count);
    }
}
