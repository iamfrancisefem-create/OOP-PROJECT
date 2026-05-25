package com.pms.controller;

import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.UserResponse;
import com.pms.entity.enums.RoleName;
import com.pms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PagedResponse<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully.", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully.", user));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @RequestParam RoleName role
    ) {
        UserResponse updatedUser = userService.updateRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully.", updatedUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String profileImage
    ) {
        UserResponse updatedUser = userService.updateUser(id, fullName, phone, profileImage);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully.", updatedUser));
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        userService.changePassword(id, oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully.", null));
    }
}
