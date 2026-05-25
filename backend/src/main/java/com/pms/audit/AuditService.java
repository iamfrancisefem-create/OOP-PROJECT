package com.pms.audit;

import com.pms.entity.AuditLog;
import com.pms.entity.User;
import com.pms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(String action, String entityType, Long entityId, String details, String ipAddress) {
        User currentUser = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            currentUser = (User) authentication.getPrincipal();
        }

        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress != null ? ipAddress : "SYSTEM")
                .user(currentUser)
                .build();

        auditLogRepository.save(log);
    }
}
