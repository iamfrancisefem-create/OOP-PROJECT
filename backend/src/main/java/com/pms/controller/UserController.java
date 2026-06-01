package com.pms.controller;

import com.pms.dto.request.ChangePasswordRequest;
import com.pms.dto.request.RegisterRequest;
import com.pms.dto.request.UpdateRoleRequest;
import com.pms.dto.request.UpdateUserRequest;
import com.pms.dto.response.ApiResponse;
import com.pms.dto.response.PagedResponse;
import com.pms.dto.response.UserResponse;
import com.pms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
// OOP — ENCAPSULATION: hides user management REST endpoints
// OOP — COMPOSITION: composed of UserService
public class UserController {

    private final UserService userService;




    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse user = userService.createUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone(),
                request.getRole()
        );
        return ResponseEntity.ok(ApiResponse.success("User created successfully.", user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        UserResponse updatedUser = userService.updateRole(id, request.getRole());
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully.", updatedUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> removeUserRole(@PathVariable Long id) {
        UserResponse updatedUser = userService.removeRole(id);
        return ResponseEntity.ok(ApiResponse.success("User role removed successfully.", updatedUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse updatedUser = userService.updateUser(id, request.getFullName(), request.getPhone(), request.getProfileImage());
        return ResponseEntity.ok(ApiResponse.success("User updated successfully.", updatedUser));
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully.", null));
    }
}
