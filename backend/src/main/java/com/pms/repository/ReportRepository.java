package com.pms.repository;

import com.pms.entity.Project;
import com.pms.entity.Report;
import com.pms.entity.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Report} entities.
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByProject(Project project, Pageable pageable);

    Page<Report> findByReportType(ReportType reportType, Pageable pageable);
}
