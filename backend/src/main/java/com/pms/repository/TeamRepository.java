package com.pms.repository;

import com.pms.entity.Team;
import com.pms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Team} entities.
 */
@Repository
// OOP — INHERITANCE: extends JpaRepository
// OOP — ABSTRACTION: abstracts team DB queries
public interface TeamRepository extends JpaRepository<Team, Long> {

    Page<Team> findByCreatedBy(User createdBy, Pageable pageable);

    Page<Team> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /** Scope teams to those the user created or is a member of. */
    @Query("SELECT t FROM Team t WHERE t.createdBy.id = :userId OR EXISTS (SELECT tm FROM TeamMember tm WHERE tm.team = t AND tm.user.id = :userId)")
    Page<Team> findByUserAccess(@Param("userId") Long userId, Pageable pageable);
}
