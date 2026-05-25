package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a team — a group of users that can be assigned to projects.
 *
 * <p>A team is created by a user (typically a PROJECT_MANAGER or ADMIN)
 * and has a collection of {@link TeamMember} entries that track
 * who belongs to the team and in what capacity.</p>
 */
@Entity
@Table(name = "teams")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Team extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /** The user who originally created this team. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /** Members of this team — cascaded so removing a team removes memberships. */
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeamMember> members = new ArrayList<>();

    /** Projects assigned to this team. */
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Project> projects = new ArrayList<>();
}
