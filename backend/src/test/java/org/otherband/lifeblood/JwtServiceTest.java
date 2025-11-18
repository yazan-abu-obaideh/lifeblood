package org.otherband.lifeblood;

import io.jsonwebtoken.security.SignatureException;
import org.apache.commons.lang3.RandomStringUtils;
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
import java.util.UUID;

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
        TimeService timeService = timeServiceMock();
        JwtService jwtService = buildJwtService(timeService);
        String refreshToken = jwtService.generateRefreshToken(User.builder().username("user").password("password").build(),
                UUID.randomUUID().toString());

        assertTrue(jwtService.isValidRefreshToken("user", refreshToken));
        assertTrue(jwtService.isValidToken(refreshToken)); // refresh token must be a valid token to pass through the filter

        assertFalse(jwtService.isValidRefreshToken("different-user", refreshToken));

        // expired
        when(timeService.now()).thenReturn(LocalDateTime.now().plusDays(45));
        assertFalse(jwtService.isValidRefreshToken("user", refreshToken));
    }

    @Test
    void generateRefreshTokenWithWrongKey() {
        JwtService jwtService = buildJwtService(new TimeService());
        JwtService other = buildJwtService(randomKey(), new TimeService());

        UserDetails userDetails = buildUserDetails(Set.of());
        String refreshToken = other.generateRefreshToken(userDetails, UUID.randomUUID().toString());

        assertThrows(SignatureException.class, () -> jwtService.isValidRefreshToken(userDetails.getUsername(), refreshToken));
        assertTrue(other.isValidRefreshToken(userDetails.getUsername(), refreshToken));
    }

    @Test
    void regularTokenIsNotValidRefreshToken() {
        JwtService jwtService = buildJwtService(new TimeService());
        UserDetails userDetails = buildUserDetails(Set.of());
        String token = jwtService.generateToken(userDetails, UUID.randomUUID().toString());
        assertFalse(jwtService.isValidRefreshToken(userDetails.getUsername(), token));
    }


    @Test
    void generateWithDifferentKey() {
        JwtService jwtService = buildJwtService(new TimeService());

        JwtService otherService = buildJwtService(randomKey(), new TimeService());

        UserDetails userDetails = buildUserDetails(Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        String token = otherService.generateToken(userDetails, UUID.randomUUID().toString());

        assertFalse(jwtService.isValidToken(token));

        assertThrows(SignatureException.class, () -> jwtService.extractUsername(token));
        assertThrows(SignatureException.class, () -> jwtService.extractRoles(token));
    }

    @Test
    void testGenerateAndExtractToken() {
        final LocalDateTime startTime = LocalDateTime.now();
        TimeService timeService = timeServiceMock();

        JwtService jwtService = buildJwtService(timeService);

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

        String userUuid = UUID.randomUUID().toString();
        String token = jwtService.generateToken(userDetails, userUuid);

        String extractedUsername = jwtService.extractUsername(token);
        List<String> extractedRoles = jwtService.extractRoles(token);
        boolean isValid = jwtService.isValidToken(token);

        assertEquals(username, extractedUsername);
        assertEquals(userUuid, jwtService.extractUuid(token));
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), extractedRoles);
        assertTrue(isValid);

        when(timeService.now()).thenReturn(startTime.plusMinutes(16)); // expired
        assertFalse(jwtService.isValidToken(token));
    }

    private static JwtService buildJwtService(TimeService timeService) {
        return buildJwtService(SECRET_KEY, timeService);
    }

    private static String randomKey() {
        return RandomStringUtils.secure().nextAlphabetic(350);
    }

    private static JwtService buildJwtService(String secretKey, TimeService timeService) {
        return new JwtService(secretKey, 15, timeService, repoMock());
    }

    private static TimeService timeServiceMock() {
        TimeService timeService = mock(TimeService.class);
        when(timeService.now()).thenCallRealMethod();
        when(timeService.getZoneId()).thenCallRealMethod();
        when(timeService.toDate(any())).thenCallRealMethod();
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