package com.pms.repository;

import com.pms.entity.Project;
import com.pms.entity.Report;
import com.pms.entity.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Report} entities.
 */
@Repository
// OOP — INHERITANCE: extends JpaRepository
// OOP — ABSTRACTION: abstracts report queries with user-access scoping
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByProject(Project project, Pageable pageable);

    Page<Report> findByReportType(ReportType reportType, Pageable pageable);

    /** Scope reports to projects the user has access to (creator or team member). */
    @Query("SELECT r FROM Report r WHERE r.project.id IN (SELECT p.id FROM Project p WHERE p.createdBy.id = :userId OR (p.team IS NOT NULL AND EXISTS (SELECT tm FROM TeamMember tm WHERE tm.team = p.team AND tm.user.id = :userId)))")
    Page<Report> findByUserAccess(@Param("userId") Long userId, Pageable pageable);
}
