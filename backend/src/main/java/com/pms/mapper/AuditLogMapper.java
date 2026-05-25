package com.pms.mapper;

import com.pms.dto.response.AuditLogResponse;
import com.pms.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);
}
