package com.pms.service.impl;

import com.pms.dto.request.LoginRequest;
import com.pms.dto.request.RegisterRequest;
import com.pms.dto.request.ResetPasswordRequest;
import com.pms.dto.response.AuthResponse;
import com.pms.entity.Role;
import com.pms.entity.User;
import com.pms.entity.enums.RoleName;
import com.pms.exception.BadRequestException;
import com.pms.exception.DuplicateResourceException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.mapper.UserMapper;
import com.pms.repository.RoleRepository;
import com.pms.repository.UserRepository;
import com.pms.security.JwtService;
import com.pms.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email address already registered: " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Enable by default for seamless developer/testing experience
        user.setEnabled(true);
        user.setEmailVerified(true);

        Role userRole = roleRepository.findByName(RoleName.TEAM_MEMBER)
                .orElseGet(() -> {
                    Role newRole = Role.builder().name(RoleName.TEAM_MEMBER).build();
                    return roleRepository.save(newRole);
                });

        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));
        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        log.info("Successfully registered user: {}", savedUser.getEmail());
        sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());

        return AuthResponse.builder()
                .id(savedUser.getId())
                .token(accessToken)
                .refreshToken(refreshToken)
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .phone(savedUser.getPhone())
                .roles(savedUser.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!user.isEnabled()) {
            throw new BadRequestException("User account is disabled. Please verify your email first.");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Successfully authenticated user: {}", user.getEmail());

        return AuthResponse.builder()
                .id(user.getId())
                .token(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        if (email == null) {
            throw new BadRequestException("Invalid refresh token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BadRequestException("Expired or invalid refresh token");
        }

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .id(user.getId())
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Generate mock reset token (or actual JWT token with shorter expiry)
        String resetToken = jwtService.generateToken(user);
        log.info("Generated forgot-password reset token for user: {}", email);
        sendPasswordResetEmail(email, resetToken);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = jwtService.extractUsername(request.getToken());
        if (email == null) {
            throw new BadRequestException("Invalid reset token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!jwtService.isTokenValid(request.getToken(), user)) {
            throw new BadRequestException("Expired or invalid reset token");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Successfully reset password for user: {}", email);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        String email = jwtService.extractUsername(token);
        if (email == null) {
            throw new BadRequestException("Invalid email verification token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!jwtService.isTokenValid(token, user)) {
            throw new BadRequestException("Expired or invalid verification token");
        }

        user.setEnabled(true);
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Successfully verified email for user: {}", email);
    }

    private void sendWelcomeEmail(String email, String name) {
        try {
            if (mailSender == null) {
                log.info("Mail sender not configured. Welcome log sent to Console for: {}", email);
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Welcome to Project Management System!");
            message.setText("Hello " + name + ",\n\nWelcome to our platform. Your account is fully active and ready.");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", email, e.getMessage());
        }
    }

    private void sendPasswordResetEmail(String email, String token) {
        try {
            if (mailSender == null) {
                log.info("Mail sender not configured. Password Reset token in console: \n{}", token);
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset Request");
            message.setText("Click the following link to reset your password:\n" +
                    "http://localhost:3000/reset-password?token=" + token);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
        }
    }
}
