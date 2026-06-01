package com.pms.service;

import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.UserResponse;
import com.pms.entity.enums.RoleName;
import org.springframework.data.domain.Pageable;

// OOP — ABSTRACTION: Contract for user CRUD and role management
public interface UserService {
    PagedResponse<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse createUser(String fullName, String email, String password, String phone, String role);
    UserResponse updateUser(Long id, String fullName, String phone, String profileImage);
    UserResponse updateRole(Long id, RoleName roleName);
    UserResponse removeRole(Long id);
    void changePassword(Long id, String oldPassword, String newPassword);
}
