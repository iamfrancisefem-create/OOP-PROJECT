package com.pms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// OOP — ENCAPSULATION: private fields with Lombok @Data — data carrier for API layer
public class AuthResponse {
    private Long id;
    private String token;
    private String refreshToken;
    private String email;
    private String fullName;
    private String phone;
    private Set<String> roles;
}
