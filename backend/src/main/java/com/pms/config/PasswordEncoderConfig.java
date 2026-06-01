package com.pms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
// OOP — ENCAPSULATION: wraps BCryptPasswordEncoder creation in a @Bean method, hiding encoding algorithm details
// OOP — ABSTRACTION: returns PasswordEncoder interface — callers depend on abstraction, not implementation
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
