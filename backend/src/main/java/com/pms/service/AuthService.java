package com.pms.service;

import com.pms.dto.request.LoginRequest;
import com.pms.dto.request.RegisterRequest;
import com.pms.dto.request.ResetPasswordRequest;
import com.pms.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequest request);
    void verifyEmail(String token);
}
