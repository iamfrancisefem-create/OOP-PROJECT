package com.pms.service.impl;

import com.pms.dto.response.FileUploadResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.entity.FileUpload;
import com.pms.entity.Project;
import com.pms.entity.User;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.FileMapper;
import com.pms.repository.FileUploadRepository;
import com.pms.repository.ProjectRepository;
import com.pms.repository.UserRepository;
import com.pms.service.FileService;
import com.pms.storage.FileStorageService;
import com.pms.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileUploadRepository fileUploadRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final FileUtil fileUtil;
    private final FileMapper fileMapper;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional
    public FileUploadResponse uploadFile(Long projectId, MultipartFile file) {
        fileUtil.validateFile(file);
        
        User user = getCurrentAuthenticatedUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        String originalName = file.getOriginalFilename();
        
        // Calculate file versioning dynamically for the project
        List<FileUpload> existingFiles = fileUploadRepository
                .findByProjectAndFileNameOrderByVersionDesc(project, originalName);
        
        int version = existingFiles.stream()
                .mapToInt(FileUpload::getVersion)
                .max()
                .orElse(0) + 1;

        // Save physical file to disk
        String storedName = fileStorageService.storeFile(file);

        FileUpload fileUpload = FileUpload.builder()
                .fileName(originalName)
                .filePath(storedName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .version(version)
                .project(project)
                .uploadedBy(user)
                .build();

        FileUpload savedFile = fileUploadRepository.save(fileUpload);
        return fileMapper.toResponse(savedFile);
    }

    @Override
    public Resource downloadFile(Long fileId) {
        FileUpload fileUpload = fileUploadRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));
        
        return fileStorageService.loadFileAsResource(fileUpload.getFilePath());
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        FileUpload fileUpload = fileUploadRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));

        // Delete physical file on disk
        fileStorageService.deleteFile(fileUpload.getFilePath());
        
        // Delete record in DB
        fileUploadRepository.delete(fileUpload);
    }

    @Override
    public PagedResponse<FileUploadResponse> getFilesByProject(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        Page<FileUpload> page = fileUploadRepository.findByProject(project, pageable);
        List<FileUploadResponse> content = page.getContent().stream()
                .map(fileMapper::toResponse)
                .toList();

        return PagedResponse.<FileUploadResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
