package com.pms.repository;

import com.pms.entity.Project;
import com.pms.entity.Task;
import com.pms.entity.User;
import com.pms.entity.enums.TaskPriority;
import com.pms.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link Task} entities — the most query-heavy repository
 * because tasks drive dashboards, reports, and notifications.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByProject(Project project, Pageable pageable);

    List<Task> findByProject(Project project);

    Page<Task> findByAssignedTo(User user, Pageable pageable);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByProjectAndStatus(Project project, TaskStatus status, Pageable pageable);

    // ---- Count queries for dashboard ----

    long countByProject(Project project);

    long countByProjectAndStatus(Project project, TaskStatus status);

    long countByAssignedTo(User user);

    long countByAssignedToAndStatus(User user, TaskStatus status);

    long countByStatus(TaskStatus status);

    long countByPriority(TaskPriority priority);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo = :user AND t.status = :status")
    long countByAssignedToAndStatusQuery(@Param("user") User user, @Param("status") TaskStatus status);

    // ---- Overdue / deadline queries ----

    /** Tasks past their deadline that are not yet DONE. */
    @Query("SELECT t FROM Task t WHERE t.deadline < :today AND t.status <> 'DONE'")
    List<Task> findOverdueTasks(@Param("today") LocalDate today);

    /** Tasks with deadlines approaching within the given number of days. */
    @Query("SELECT t FROM Task t WHERE t.deadline BETWEEN :today AND :threshold AND t.status <> 'DONE'")
    List<Task> findTasksApproachingDeadline(
            @Param("today") LocalDate today,
            @Param("threshold") LocalDate threshold);

    /** Count overdue tasks across the entire system. */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.deadline < :today AND t.status <> 'DONE'")
    long countOverdueTasks(@Param("today") LocalDate today);
}
