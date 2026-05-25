package com.pms.entity;

import com.pms.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * In-app notification delivered to a user.
 *
 * <p>Notifications are created by the system (e.g. task assignment,
 * approaching deadline) and optionally mirrored as email alerts.
 * The {@code seen} flag tracks whether the user has acknowledged them.</p>
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Builder.Default
    @Column(nullable = false)
    private Boolean seen = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
