package com.pms.repository;

import com.pms.entity.Team;
import com.pms.entity.TeamMember;
import com.pms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link TeamMember} entities.
 */
@Repository
// OOP — INHERITANCE: extends JpaRepository
// OOP — ABSTRACTION: abstracts team-membership mapping queries
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByTeam(Team team);

    List<TeamMember> findByUser(User user);

    Optional<TeamMember> findByTeamAndUser(Team team, User user);

    boolean existsByTeamAndUser(Team team, User user);

    void deleteByTeamAndUser(Team team, User user);

    @Query("SELECT COUNT(DISTINCT tm.user.id) FROM TeamMember tm WHERE tm.team.id IN (SELECT p.team.id FROM Project p WHERE (p.createdBy.id = :userId OR (p.team IS NOT NULL AND EXISTS (SELECT tm2 FROM TeamMember tm2 WHERE tm2.team = p.team AND tm2.user.id = :userId))) AND p.team IS NOT NULL)")
    long countDistinctUsersByUserAccess(@Param("userId") Long userId);
}
