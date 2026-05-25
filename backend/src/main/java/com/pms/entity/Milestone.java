package com.pms.entity;

import com.pms.entity.enums.MilestoneStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Represents a project milestone — a key deliverable or checkpoint.
 *
 * <p>Milestones have deadlines and are tracked independently of tasks.
 * They provide a higher-level view of project progress.</p>
 */
@Entity
@Table(name = "milestones")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Milestone extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MilestoneStatus status = MilestoneStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}
