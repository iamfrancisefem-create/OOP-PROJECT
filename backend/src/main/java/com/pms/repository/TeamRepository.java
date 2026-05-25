package com.pms.repository;

import com.pms.entity.Team;
import com.pms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Team} entities.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Page<Team> findByCreatedBy(User createdBy, Pageable pageable);

    Page<Team> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
