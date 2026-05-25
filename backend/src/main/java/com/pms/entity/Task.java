package com.pms.entity;

import com.pms.entity.enums.TaskPriority;
import com.pms.entity.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Central work-item entity. Tasks belong to a project, are assigned
 * to a user, and progress through the workflow:
 * {@code TODO → IN_PROGRESS → TESTING → DONE}.
 *
 * <p>Tasks are the primary unit of work tracked by the dashboard,
 * reports, and notification systems.</p>
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Task extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    private LocalDate deadline;

    // ------------------------------------------------------------------ //
    // Relationships                                                      //
    // ------------------------------------------------------------------ //

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** The user currently responsible for this task (nullable = unassigned). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    /** The user who originally created this task. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
}
