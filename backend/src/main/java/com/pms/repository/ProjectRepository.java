package com.pms.repository;

import com.pms.entity.Project;
import com.pms.entity.Team;
import com.pms.entity.User;
import com.pms.entity.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Project} entities.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByCreatedBy(User createdBy, Pageable pageable);

    Page<Project> findByTeam(Team team, Pageable pageable);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    List<Project> findByStatus(ProjectStatus status);

    Page<Project> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    long countByStatus(ProjectStatus status);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.createdBy = :user")
    long countByCreatedBy(@Param("user") User user);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.team = :team")
    long countByTeam(@Param("team") Team team);
}
