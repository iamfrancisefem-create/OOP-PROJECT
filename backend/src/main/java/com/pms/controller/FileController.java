package com.pms.controller;

import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.FileUploadResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.entity.FileUpload;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.FileUploadRepository;
import com.pms.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileUploadRepository fileUploadRepository;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam Long projectId,
            @RequestParam("file") MultipartFile file
    ) {
        FileUploadResponse response = fileService.uploadFile(projectId, file);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully.", response));
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Resource resource = fileService.downloadFile(id);
        
        FileUpload fileUpload = fileUploadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File details not found in database"));

        String contentType = fileUpload.getFileType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileUpload.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully from disk and registry.", null));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<PagedResponse<FileUploadResponse>>> getFilesByProject(
            @PathVariable Long projectId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PagedResponse<FileUploadResponse> response = fileService.getFilesByProject(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Project files registry retrieved successfully.", response));
    }
}
