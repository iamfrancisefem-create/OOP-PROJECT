package com.pms.repository;

import com.pms.entity.Team;
import com.pms.entity.TeamMember;
import com.pms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link TeamMember} entities.
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByTeam(Team team);

    List<TeamMember> findByUser(User user);

    Optional<TeamMember> findByTeamAndUser(Team team, User user);

    boolean existsByTeamAndUser(Team team, User user);

    void deleteByTeamAndUser(Team team, User user);
}
