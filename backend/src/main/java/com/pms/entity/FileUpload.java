package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Tracks files uploaded to the system, associated with a project.
 *
 * <p>Supports basic file versioning — when a file with the same name
 * is uploaded again, a new {@link FileUpload} record is created with
 * an incremented {@code version} number.</p>
 */
@Entity
@Table(name = "file_uploads")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FileUpload extends BaseEntity {

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    /** Version number — incremented on re-upload of the same logical file. */
    @Builder.Default
    @Column(nullable = false)
    private Integer version = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
}
