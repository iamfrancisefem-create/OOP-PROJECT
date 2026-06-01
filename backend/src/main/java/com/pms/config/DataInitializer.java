package com.pms.config;

import com.pms.entity.Role;
import com.pms.entity.User;
import com.pms.entity.enums.RoleName;
import com.pms.repository.RoleRepository;
import com.pms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Initializes database with default roles and mock users if empty.
 */
@Component
@RequiredArgsConstructor
@Slf4j
// OOP — INHERITANCE: implements CommandLineRunner (Spring Boot contract for startup logic)
// OOP — POLYMORPHISM: provides concrete implementation of run() that Spring calls polymorphically
// OOP — ENCAPSULATION: hides DB seeding logic behind a single component
// OOP — COMPOSITION: composed of UserRepository, RoleRepository, PasswordEncoder
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Starting database initialization...");

        // 1. Seed Roles if missing
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = Role.builder().name(roleName).build();
                roleRepository.save(role);
                log.info("Seeded role: {}", roleName);
            }
        }

        // 2. Seed Mock Users if user table contains fewer than 3 users
        if (userRepository.count() < 3) {
            log.info("Seeding mock users to enable chat contacts...");
            
            Role memberRole = roleRepository.findByName(RoleName.TEAM_MEMBER)
                    .orElseThrow(() -> new IllegalStateException("TEAM_MEMBER role not found"));
            Role managerRole = roleRepository.findByName(RoleName.PROJECT_MANAGER)
                    .orElseThrow(() -> new IllegalStateException("PROJECT_MANAGER role not found"));
            Role leaderRole = roleRepository.findByName(RoleName.TEAM_LEADER)
                    .orElseThrow(() -> new IllegalStateException("TEAM_LEADER role not found"));
            Role ownerRole = roleRepository.findByName(RoleName.PRODUCT_OWNER)
                    .orElseThrow(() -> new IllegalStateException("PRODUCT_OWNER role not found"));
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

            List<User> mockUsers = List.of(
                    User.builder()
                            .fullName("Jane Cooper")
                            .email("jane@example.com")
                            .password(passwordEncoder.encode("password123"))
                            .phone("+1-202-555-0143")
                            .enabled(true)
                            .emailVerified(true)
                            .roles(new HashSet<>(Collections.singletonList(memberRole)))
                            .build(),
                    User.builder()
                            .fullName("John Doe")
                            .email("john@example.com")
                            .password(passwordEncoder.encode("password123"))
                            .phone("+1-202-555-0155")
                            .enabled(true)
                            .emailVerified(true)
                            .roles(new HashSet<>(Collections.singletonList(managerRole)))
                            .build(),
                    User.builder()
                            .fullName("Robert Fox")
                            .email("robert@example.com")
                            .password(passwordEncoder.encode("password123"))
                            .phone("+1-202-555-0177")
                            .enabled(true)
                            .emailVerified(true)
                            .roles(new HashSet<>(Collections.singletonList(leaderRole)))
                            .build(),
                    User.builder()
                            .fullName("Esther Howard")
                            .email("esther@example.com")
                            .password(passwordEncoder.encode("password123"))
                            .phone("+1-202-555-0199")
                            .enabled(true)
                            .emailVerified(true)
                            .roles(new HashSet<>(Collections.singletonList(ownerRole)))
                            .build(),
                    User.builder()
                            .fullName("Albert Flores")
                            .email("albert@example.com")
                            .password(passwordEncoder.encode("password123"))
                            .phone("+1-555-0122")
                            .enabled(true)
                            .emailVerified(true)
                            .roles(new HashSet<>(Collections.singletonList(memberRole)))
                            .build(),
                    User.builder()
                            .fullName("System Admin")
                            .email("admin@pms.com")
                            .password(passwordEncoder.encode("admin123"))
                            .phone("+1-555-0000")
                            .enabled(true)
                            .emailVerified(true)
                            .roles(new HashSet<>(Collections.singletonList(adminRole)))
                            .build()
            );

            for (User u : mockUsers) {
                if (userRepository.findByEmail(u.getEmail()).isEmpty()) {
                    userRepository.save(u);
                    log.info("Seeded mock user: {}", u.getFullName());
                }
            }
        }
        log.info("Database initialization completed successfully.");
    }
}
