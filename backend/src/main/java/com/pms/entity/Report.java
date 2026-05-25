package com.pms.entity;

import com.pms.entity.enums.ReportType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Represents a generated report (PDF or Excel).
 *
 * <p>Reports are created on-demand or by the scheduled report generator.
 * The {@code fileUrl} points to the stored report file, and
 * {@code parameters} stores the JSON-encoded generation parameters
 * (e.g. date range, filters) for reproducibility.</p>
 */
@Entity
@Table(name = "reports")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 30)
    private ReportType reportType;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    /** JSON-encoded generation parameters for audit/reproducibility. */
    @Column(columnDefinition = "TEXT")
    private String parameters;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private User generatedBy;
}
