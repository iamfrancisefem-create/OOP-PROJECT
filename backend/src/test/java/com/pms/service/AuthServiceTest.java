package com.pms.service;

import com.pms.dto.request.RegisterRequest;
import com.pms.dto.response.AuthResponse;
import com.pms.entity.Role;
import com.pms.entity.User;
import com.pms.entity.enums.RoleName;
import com.pms.exception.DuplicateResourceException;
import com.pms.mapper.UserMapper;
import com.pms.repository.RoleRepository;
import com.pms.repository.UserRepository;
import com.pms.security.JwtService;
import com.pms.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    public void testRegister_Success() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("new@example.com")
                .fullName("New User")
                .password("password123")
                .build();

        User user = User.builder()
                .email("new@example.com")
                .fullName("New User")
                .build();

        Role role = Role.builder().name(RoleName.TEAM_MEMBER).build();
        user.setRoles(new HashSet<>(Collections.singletonList(role)));

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-pwd");
        when(roleRepository.findByName(RoleName.TEAM_MEMBER)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("new@example.com", response.getEmail());
        
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegister_DuplicateEmail_ShouldThrowException() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("duplicate@example.com")
                .fullName("Duplicate User")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }
}
