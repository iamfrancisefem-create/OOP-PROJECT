package com.pms.repository;

import com.pms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link User} entities.
 */
@Repository
// OOP — INHERITANCE: extends JpaRepository (inherits CRUD, pagination, sorting)
// OOP — ABSTRACTION: abstracts away DB access behind method names and query derivation
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    Page<User> findByEnabledTrue(Pageable pageable);

    Page<User> findByCreatedBy(User createdBy, Pageable pageable);

    long countByCreatedBy(User createdBy);
}
