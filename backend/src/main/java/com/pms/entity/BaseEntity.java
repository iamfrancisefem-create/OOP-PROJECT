package com.pms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Abstract base entity providing common fields for all JPA entities.
 *
 * <p>Uses Spring Data JPA auditing to automatically populate
 * {@code createdAt} and {@code updatedAt} timestamps on persist and update.</p>
 *
 * <p>All concrete entities should extend this class to inherit the
 * auto-generated primary key and audit timestamps.</p>
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Timestamp set automatically when the entity is first persisted. */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp updated automatically on every entity modification. */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
