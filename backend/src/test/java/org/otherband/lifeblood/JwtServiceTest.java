package org.otherband.lifeblood;

import org.junit.jupiter.api.Test;
import org.otherband.lifeblood.auth.JwtService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void testGenerateAndExtractToken() {
        String secretKey = Base64.getEncoder().encodeToString(
                "test-secret-key-must-be-at-least-256-bits-long-for-security".getBytes()
        );
        JwtService jwtService = new JwtService(secretKey, 15);

        String username = "testuser";
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(authorities)
                .build();

        String token = jwtService.generateToken(userDetails);

        String extractedUsername = jwtService.extractUsername(token);
        List<String> extractedRoles = jwtService.extractRoles(token);
        boolean isValid = jwtService.validateToken(token, userDetails);

        assertEquals(username, extractedUsername);
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), extractedRoles);
        assertTrue(isValid);

    }
}