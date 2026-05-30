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
    private final com.pms.repository.TeamMemberRepository teamMemberRepository;

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private void validateProjectAccess(Project project) {
        User currentUser = getCurrentAuthenticatedUser();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ADMIN"));
        boolean isCreator = project.getCreatedBy() != null && project.getCreatedBy().getId().equals(currentUser.getId());
        boolean isTeamMember = project.getTeam() != null && teamMemberRepository.existsByTeamAndUser(project.getTeam(), currentUser);

        if (!isAdmin && !isCreator && !isTeamMember) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not have permission to access files for this project.");
        }
    }

    @Override
    @Transactional
    public FileUploadResponse uploadFile(Long projectId, MultipartFile file) {
        fileUtil.validateFile(file);
        
        User user = getCurrentAuthenticatedUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        validateProjectAccess(project);

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
        
        validateProjectAccess(fileUpload.getProject());
        
        return fileStorageService.loadFileAsResource(fileUpload.getFilePath());
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        FileUpload fileUpload = fileUploadRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));

        User user = getCurrentAuthenticatedUser();
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().name().equals("ADMIN"));
        boolean isProjectCreator = fileUpload.getProject().getCreatedBy() != null && fileUpload.getProject().getCreatedBy().getId().equals(user.getId());
        boolean isUploader = fileUpload.getUploadedBy() != null && fileUpload.getUploadedBy().getId().equals(user.getId());

        if (!isAdmin && !isProjectCreator && !isUploader) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: Only the uploader, project creator, or ADMIN can delete files.");
        }

        // Delete physical file on disk
        fileStorageService.deleteFile(fileUpload.getFilePath());
        
        // Delete record in DB
        fileUploadRepository.delete(fileUpload);
    }

    @Override
    public PagedResponse<FileUploadResponse> getFilesByProject(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        validateProjectAccess(project);

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
