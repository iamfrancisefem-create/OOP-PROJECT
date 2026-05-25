package com.pms.mapper;

import com.pms.dto.request.RegisterRequest;
import com.pms.dto.response.UserResponse;
import com.pms.entity.Role;
import com.pms.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponse toResponse(User user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    User toEntity(RegisterRequest request);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
