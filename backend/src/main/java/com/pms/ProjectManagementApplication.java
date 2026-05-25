package com.pms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Project Management System application.
 *
 * <p>{@code @EnableJpaAuditing} activates automatic population of
 * {@code createdAt} / {@code updatedAt} fields on entities that use
 * {@link org.springframework.data.annotation.CreatedDate} and
 * {@link org.springframework.data.annotation.LastModifiedDate}.</p>
 *
 * <p>{@code @EnableScheduling} enables Spring's scheduled-task execution
 * for deadline reminders and recurring report generation.</p>
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class ProjectManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectManagementApplication.class, args);
    }
}
