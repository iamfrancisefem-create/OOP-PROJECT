package com.pms.service;

import com.pms.dto.request.LoginRequest;
import com.pms.dto.request.RegisterRequest;
import com.pms.dto.request.ResetPasswordRequest;
import com.pms.dto.response.AuthResponse;

// OOP — ABSTRACTION: Defines a contract for authentication operations without exposing implementation details
// OOP — POLYMORPHISM: Multiple implementations can satisfy this interface (e.g. mock for tests)
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequest request);
    void verifyEmail(String token);
}
