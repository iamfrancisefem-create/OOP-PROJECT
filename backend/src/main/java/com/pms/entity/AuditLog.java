package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Immutable audit trail entry recording a user action in the system.
 *
 * <p>Audit logs capture who did what, when, and from where.
 * They are write-only — once created, they should never be modified
 * or deleted (enforced at the service layer).</p>
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseEntity {

    /** The action performed (e.g. "CREATE", "UPDATE", "DELETE", "LOGIN"). */
    @Column(nullable = false, length = 50)
    private String action;

    /** The type of entity affected (e.g. "Project", "Task", "User"). */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /** The ID of the affected entity (nullable for non-entity actions). */
    @Column(name = "entity_id")
    private Long entityId;

    /** Human-readable details or JSON payload describing the change. */
    @Column(columnDefinition = "TEXT")
    private String details;

    /** IPv4 or IPv6 address of the request origin. */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** The user who performed the action (nullable for system actions). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
