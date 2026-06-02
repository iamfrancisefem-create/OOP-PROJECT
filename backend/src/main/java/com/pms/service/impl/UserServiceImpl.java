package com.pms.service.impl;

import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.UserResponse;
import com.pms.entity.Role;
import com.pms.entity.User;
import com.pms.entity.enums.RoleName;
import com.pms.exception.BadRequestException;
import com.pms.exception.DuplicateResourceException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.UserMapper;
import com.pms.repository.RoleRepository;
import com.pms.repository.UserRepository;
import com.pms.service.EmailService;
import com.pms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.EntityManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
// OOP — INHERITANCE: implements UserService interface
// OOP — ENCAPSULATION: hides user CRUD, role assignment, and password-change logic
// OOP — COMPOSITION: composed of UserRepository, RoleRepository, UserMapper, PasswordEncoder
public class UserServiceImpl implements UserService {

    @Value("${app.invite-link-base:https://oop-project-tau.vercel.app}")
    private String inviteLinkBase;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final EmailService emailService;

    @Override
    public PagedResponse<UserResponse> getAllUsers(Pageable pageable) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Page<User> usersPage = userRepository.findByCreatedBy(currentUser, pageable);
        List<UserResponse> content = usersPage.getContent().stream()
                .map(userMapper::toResponse)
                .toList();

        return PagedResponse.<UserResponse>builder()
                .content(content)
                .page(usersPage.getNumber())
                .size(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .last(usersPage.isLast())
                .build();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(String fullName, String email, String password, String phone, String role) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email address already registered: " + email);
        }

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .enabled(true)
                .emailVerified(true)
                .build();

        RoleName roleName = RoleName.TEAM_MEMBER;
        if (role != null && !role.isBlank()) {
            try {
                roleName = RoleName.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        final RoleName finalRole = roleName;
        Role userRole = roleRepository.findByName(finalRole)
                .orElseGet(() -> {
                    Role newRole = Role.builder().name(finalRole).build();
                    return roleRepository.save(newRole);
                });

        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByEmail(currentEmail).ifPresent(user::setCreatedBy);

        User savedUser = userRepository.save(user);

        String loginLink = inviteLinkBase + "/pages/login.html";

        emailService.sendEmail(
                email,
                "Welcome to ProjectFlow — Account Created",
                "Hello " + fullName + ",\n\nAn admin has created an account for you on ProjectFlow.\n\n" +
                "Your login email: " + email + "\n" +
                "Please use the password provided by your admin to sign in.\n\n" +
                "Login here: " + loginLink + "\n\n" +
                "Best regards,\nThe ProjectFlow Team"
        );

        UserResponse response = userMapper.toResponse(savedUser);
        response.setSetupLink(loginLink);
        return response;
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, String fullName, String phone, String profileImage) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (profileImage != null) {
            user.setProfileImage(profileImage);
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateRole(Long id, RoleName roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = Role.builder().name(roleName).build();
                    return roleRepository.save(newRole);
                });

        user.getRoles().clear();
        user.getRoles().add(role);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse removeRole(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.getRoles().clear();
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public long countUsersCreatedByMe() {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userRepository.countByCreatedBy(currentUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.getRoles().clear();

        entityManager.createNativeQuery("UPDATE projects SET created_by = NULL WHERE created_by = :id")
                .setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("UPDATE teams SET created_by = NULL WHERE created_by = :id")
                .setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("UPDATE comments SET user_id = NULL WHERE user_id = :id")
                .setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM messages WHERE sender_id = :id OR receiver_id = :id")
                .setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("UPDATE file_uploads SET uploaded_by = NULL WHERE uploaded_by = :id")
                .setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("UPDATE reports SET generated_by = NULL WHERE generated_by = :id")
                .setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("UPDATE tasks SET created_by = NULL WHERE created_by = :id")
                .setParameter("id", id).executeUpdate();
        entityManager.createNativeQuery("UPDATE platform_invitations SET invited_by_id = NULL WHERE invited_by_id = :id")
                .setParameter("id", id).executeUpdate();

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
