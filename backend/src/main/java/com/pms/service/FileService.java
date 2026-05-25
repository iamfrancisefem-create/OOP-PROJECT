package com.pms.service;

import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.FileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileUploadResponse uploadFile(Long projectId, MultipartFile file);
    Resource downloadFile(Long fileId);
    void deleteFile(Long fileId);
    PagedResponse<FileUploadResponse> getFilesByProject(Long projectId, Pageable pageable);
}
