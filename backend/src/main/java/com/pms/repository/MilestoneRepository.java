package com.pms.repository;

import com.pms.entity.Milestone;
import com.pms.entity.Project;
import com.pms.entity.enums.MilestoneStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link Milestone} entities.
 */
@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByProject(Project project);

    Page<Milestone> findByProject(Project project, Pageable pageable);

    List<Milestone> findByProjectAndStatus(Project project, MilestoneStatus status);

    /** Find milestones whose deadline is today or in the past and are not yet completed. */
    @Query("SELECT m FROM Milestone m WHERE m.deadline <= :date AND m.status <> 'COMPLETED'")
    List<Milestone> findOverdueMilestones(@Param("date") LocalDate date);
}
