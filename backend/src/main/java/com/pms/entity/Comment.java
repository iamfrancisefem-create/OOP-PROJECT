package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * A comment left by a user on a specific task.
 *
 * <p>Comments enable collaboration and discussion around tasks.
 * They are orphan-removed when their parent task is deleted.</p>
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
