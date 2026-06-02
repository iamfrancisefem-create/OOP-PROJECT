package com.pms.repository;

import com.pms.entity.PlatformInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformInvitationRepository extends JpaRepository<PlatformInvitation, Long> {

    Optional<PlatformInvitation> findByToken(String token);

    Optional<PlatformInvitation> findByEmailAndAcceptedFalse(String email);

    List<PlatformInvitation> findByAcceptedFalseOrderByCreatedAtDesc();
}
