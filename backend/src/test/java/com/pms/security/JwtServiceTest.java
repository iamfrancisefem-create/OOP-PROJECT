package com.pms.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    public void setup() {
        // Inject values manually using ReflectionTestUtils to avoid requiring full application context load
        ReflectionTestUtils.setField(jwtService, "secretKey", "c2VjdXJlLWp3dC1zZWNyZXQta2V5LWZvci1wcm9qZWN0LW1hbmFnZW1lbnQtc3lzdGVt");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 900000L); // 15 minutes
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L); // 7 days
    }

    @Test
    public void testGenerateToken_ShouldSucceed() {
        UserDetails userDetails = new User("test@example.com", "encoded-password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void testExtractUsername_ShouldReturnCorrectEmail() {
        UserDetails userDetails = new User("jane.doe@example.com", "encoded-password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);
        assertEquals("jane.doe@example.com", username);
    }

    @Test
    public void testIsTokenValid_ShouldReturnTrueForMatchingUserAndActiveToken() {
        UserDetails userDetails = new User("jane.doe@example.com", "encoded-password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    public void testIsTokenValid_ShouldReturnFalseForMismatchingUser() {
        UserDetails userDetails = new User("jane.doe@example.com", "encoded-password", Collections.emptyList());
        UserDetails otherUser = new User("john.doe@example.com", "encoded-password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, otherUser);
        assertFalse(isValid);
    }
}
