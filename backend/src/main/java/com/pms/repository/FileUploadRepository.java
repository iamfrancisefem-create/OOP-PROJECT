package com.pms.repository;

import com.pms.entity.FileUpload;
import com.pms.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link FileUpload} entities.
 */
@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {

    Page<FileUpload> findByProject(Project project, Pageable pageable);

    /** Find all versions of a file by name within a project, ordered newest first. */
    List<FileUpload> findByProjectAndFileNameOrderByVersionDesc(Project project, String fileName);
}
