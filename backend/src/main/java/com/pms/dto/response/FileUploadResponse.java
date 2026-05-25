package com.pms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private Integer version;
    private Long projectId;
    private UserResponse uploadedBy;
    private LocalDateTime createdAt;
}
