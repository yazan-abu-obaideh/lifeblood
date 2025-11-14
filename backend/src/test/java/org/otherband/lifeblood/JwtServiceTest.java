package org.otherband.lifeblood;

import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;
import org.otherband.lifeblood.auth.JwtService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    @Test
    void generateWithDifferentKey() {
        String secretKey = Base64.getEncoder().encodeToString(
                "test-secret-key-must-be-at-least-256-bits-long-for-security".getBytes()
        );
        JwtService jwtService = new JwtService(secretKey, 15, new TimeService());

        JwtService otherService = new JwtService(
                "some-other-key-some-other-key-some-other-key-some-other-key-some-other-key-some-other-key",
                15, new TimeService());

        UserDetails userDetails = User.builder()
                .username("username")
                .password("password")
                .authorities(Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        String token = otherService.generateToken(userDetails);

        assertFalse(jwtService.isValidToken(token));

        assertThrows(SignatureException.class, () -> jwtService.extractUsername(token));
        assertThrows(SignatureException.class, () -> jwtService.extractRoles(token));
    }

    @Test
    void testGenerateAndExtractToken() {
        final LocalDateTime startTime = LocalDateTime.now();
        TimeService timeService = mock(TimeService.class);
        when(timeService.now()).thenCallRealMethod();
        when(timeService.getZoneId()).thenCallRealMethod();

        String secretKey = Base64.getEncoder().encodeToString(
                "test-secret-key-must-be-at-least-256-bits-long-for-security".getBytes()
        );
        JwtService jwtService = new JwtService(secretKey, 15, timeService);

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
        boolean isValid = jwtService.isValidToken(token);

        assertEquals(username, extractedUsername);
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), extractedRoles);
        assertTrue(isValid);

        when(timeService.now()).thenReturn(startTime.plusMinutes(16)); // expired
        assertFalse(jwtService.isValidToken(token));
    }
}