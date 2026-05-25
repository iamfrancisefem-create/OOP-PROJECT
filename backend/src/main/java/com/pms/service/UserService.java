package com.pms.service;

import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.UserResponse;
import com.pms.entity.enums.RoleName;
import org.springframework.data.domain.Pageable;

public interface UserService {
    PagedResponse<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, String fullName, String phone, String profileImage);
    UserResponse updateRole(Long id, RoleName roleName);
    void changePassword(Long id, String oldPassword, String newPassword);
}
