package com.pms.dto.response;

import com.pms.entity.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// OOP — ENCAPSULATION: private fields with Lombok @Data — data carrier for API layer
public class ReportResponse {
    private Long id;
    private ReportType reportType;
    private String fileUrl;
    private String parameters;
    private Long projectId;
    private UserResponse generatedBy;
    private LocalDateTime createdAt;
}
