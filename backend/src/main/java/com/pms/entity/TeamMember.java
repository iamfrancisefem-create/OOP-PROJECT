package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Join entity for the many-to-many relationship between
 * {@link User} and {@link Team}.
 *
 * <p>Modelled as an explicit entity (rather than {@code @ManyToMany})
 * because it carries extra metadata: the member's role within the team
 * and the date they joined.</p>
 */
@Entity
@Table(name = "team_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_id", "user_id"})
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Role of this member within the team (e.g. "LEADER", "DEVELOPER"). */
    @Column(length = 50)
    private String role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}
