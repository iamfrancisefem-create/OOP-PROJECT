package com.pms.repository;

import com.pms.entity.Project;
import com.pms.entity.ProjectInvitation;
import com.pms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// OOP — INHERITANCE: extends JpaRepository
// OOP — ABSTRACTION: abstracts invitation token lookups
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {

    Optional<ProjectInvitation> findByToken(String token);

    Optional<ProjectInvitation> findByProjectAndInvitedUserAndAcceptedFalse(Project project, User invitedUser);
}
