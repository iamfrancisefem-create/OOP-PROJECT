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
import org.springframework.beans.factory.annotation.Value;
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

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Value("${app.auto-verify:true}")
    private boolean autoVerify;

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

        // Auto-verify in dev mode; require email verification in production
        user.setEnabled(autoVerify);
        user.setEmailVerified(autoVerify);

        RoleName requestedRole = RoleName.TEAM_MEMBER;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                RoleName parsed = RoleName.valueOf(request.getRole().toUpperCase());
                if (parsed != RoleName.ADMIN) {
                    requestedRole = parsed;
                }
            } catch (IllegalArgumentException ignored) {}
        }

        final RoleName finalRole = requestedRole;
        Role userRole = roleRepository.findByName(finalRole)
                .orElseGet(() -> {
                    Role newRole = Role.builder().name(finalRole).build();
                    return roleRepository.save(newRole);
                });

        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));
        User savedUser = userRepository.save(user);

        String verificationToken = jwtService.generateToken(savedUser);

        String accessToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        log.info("Successfully registered user: {}", savedUser.getEmail());
        sendVerificationEmail(savedUser.getEmail(), savedUser.getFullName(), verificationToken);

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
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new BadRequestException("Account is locked due to too many failed login attempts. Please reset your password or contact support.");
        }

        if (!user.isEnabled()) {
            throw new BadRequestException("User account is disabled. Please verify your email first.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Reset failed attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                user.setAccountLocked(false);
                userRepository.save(user);
            }
        } catch (Exception e) {
            // Increment failed login attempts
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setFailedLoginAttempts(0);
                log.warn("Account locked due to {} failed login attempts for user: {}", MAX_FAILED_ATTEMPTS, user.getEmail());
            }
            userRepository.save(user);
            throw e;
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

        // Generate a dedicated short-lived reset token (separate from auth token)
        String resetToken = jwtService.generateResetToken(user);
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

    private void sendVerificationEmail(String email, String name, String token) {
        try {
            String verificationLink = "http://localhost:5500/pages/verify-email.html?token=" + token;
            if (mailSender == null) {
                log.info("Mail sender not configured. Verification link in console: \n{}", verificationLink);
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Verify your email - Project Management System");
            message.setText("Hello " + name + ",\n\nPlease verify your email by clicking the link below:\n" + verificationLink);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage());
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
