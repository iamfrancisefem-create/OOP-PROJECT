package com.pms.entity;

import com.pms.entity.enums.ProjectPriority;
import com.pms.entity.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a project with lifecycle tracking, priority, and progress.
 *
 * <p>A project belongs to a team and is created by a user.
 * Progress is calculated as a percentage based on completed tasks
 * relative to total tasks.</p>
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjectPriority priority = ProjectPriority.MEDIUM;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** Percentage of tasks completed (0.0 – 100.0). */
    @Builder.Default
    @Column(nullable = false)
    private Double progress = 0.0;

    // ------------------------------------------------------------------ //
    // Relationships                                                      //
    // ------------------------------------------------------------------ //

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Milestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FileUpload> files = new ArrayList<>();
}
