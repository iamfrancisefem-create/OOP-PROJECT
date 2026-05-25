package com.pms.repository;

import com.pms.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link AuditLog} entities.
 *
 * <p>Audit logs are append-only — the service layer should never
 * expose update or delete operations on this repository.</p>
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);
}
