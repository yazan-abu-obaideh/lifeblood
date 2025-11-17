package org.otherband.lifeblood;

import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;
import org.otherband.lifeblood.auth.JwtService;
import org.otherband.lifeblood.auth.RefreshTokenRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private static final String SECRET_KEY = Base64.getEncoder().encodeToString(
            "test-secret-key-must-be-at-least-256-bits-long-for-security".getBytes()
    );

    @Test
    void generateRefreshToken() {
        RefreshTokenRepository repository = repoMock();
        JwtService jwtService = new JwtService(SECRET_KEY, 15, new TimeService(), repository);
        String refreshToken = jwtService.generateRefreshToken(User.builder().username("user").password("password").build());
        assertTrue(jwtService.isValidRefreshToken("user", refreshToken));
        assertFalse(jwtService.isValidRefreshToken("different-user", refreshToken));
    }

    @Test
    void regularTokenIsNotValidRefreshToken() {
        JwtService jwtService = new JwtService(SECRET_KEY, 15, new TimeService(), repoMock());
        String token = jwtService.generateToken(buildUserDetails(Set.of()));
        assertFalse(jwtService.isValidRefreshToken("username", token));
    }


    @Test
    void generateWithDifferentKey() {
        JwtService jwtService = new JwtService(SECRET_KEY, 15, new TimeService(), repoMock());

        JwtService otherService = new JwtService(
                "some-other-key-some-other-key-some-other-key-some-other-key-some-other-key-some-other-key",
                15, new TimeService(),
                repoMock()
                );

        UserDetails userDetails = buildUserDetails(Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        String token = otherService.generateToken(userDetails);

        assertFalse(jwtService.isValidToken(token));

        assertThrows(SignatureException.class, () -> jwtService.extractUsername(token));
        assertThrows(SignatureException.class, () -> jwtService.extractRoles(token));
    }

    @Test
    void testGenerateAndExtractToken() {
        final LocalDateTime startTime = LocalDateTime.now();
        TimeService timeService = timeServiceMock();

        JwtService jwtService = new JwtService(SECRET_KEY, 15, timeService, repoMock());

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

    private static TimeService timeServiceMock() {
        TimeService timeService = mock(TimeService.class);
        when(timeService.now()).thenCallRealMethod();
        when(timeService.getZoneId()).thenCallRealMethod();
        return timeService;
    }

    private static UserDetails buildUserDetails(Set<SimpleGrantedAuthority> authorities) {
        return User.builder()
                .username("username")
                .password("password")
                .authorities(authorities)
                .build();
    }

    private static RefreshTokenRepository repoMock() {
        RefreshTokenRepository mock = mock();
        when(mock.save(any())).then(invocation -> invocation.getArgument(0));
        return mock;
    }
}