package org.otherband.lifeblood;

import io.jsonwebtoken.security.SignatureException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.otherband.lifeblood.auth.JwtService;
import org.otherband.lifeblood.auth.RefreshTokenRepository;
import org.otherband.lifeblood.volunteer.UserDetails;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
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
        String refreshToken = jwtService.generateRefreshToken(UserDetails.builder().phoneNumber("user").build(),
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

        UserDetails userDetails = buildUserDetails(List.of());
        String refreshToken = other.generateRefreshToken(userDetails, UUID.randomUUID().toString());

        assertThrows(SignatureException.class, () -> jwtService.isValidRefreshToken(userDetails.getPhoneNumber(), refreshToken));
        assertTrue(other.isValidRefreshToken(userDetails.getPhoneNumber(), refreshToken));
    }

    @Test
    void regularTokenIsNotValidRefreshToken() {
        JwtService jwtService = buildJwtService(new TimeService());
        UserDetails userDetails = buildUserDetails(List.of());
        String token = jwtService.generateToken(userDetails, UUID.randomUUID().toString());
        assertFalse(jwtService.isValidRefreshToken(userDetails.getPhoneNumber(), token));
    }


    @Test
    void generateWithDifferentKey() {
        JwtService jwtService = buildJwtService(new TimeService());

        JwtService otherService = buildJwtService(randomKey(), new TimeService());

        UserDetails userDetails = buildUserDetails(List.of("ROLE_ADMIN"));

        String token = otherService.generateToken(userDetails, UUID.randomUUID().toString());

        assertFalse(jwtService.isValidToken(token));

        assertThrows(SignatureException.class, () -> jwtService.extractSubject(token));
        assertThrows(SignatureException.class, () -> jwtService.extractRoles(token));
    }

    @Test
    void testGenerateAndExtractToken() {
        final LocalDateTime startTime = LocalDateTime.now();
        TimeService timeService = timeServiceMock();

        JwtService jwtService = buildJwtService(timeService);

        String phoneNumber = "079 777 7777";
        List<String> authorities = List.of(
                "ROLE_ADMIN",
                "ROLE_USER"
        );
        UserDetails userDetails = UserDetails.builder()
                .phoneNumber(phoneNumber)
                .roles(authorities)
                .build();

        String userUuid = UUID.randomUUID().toString();
        String token = jwtService.generateToken(userDetails, userUuid);

        String extractedSubject = jwtService.extractSubject(token);
        List<String> extractedRoles = jwtService.extractRoles(token);
        boolean isValid = jwtService.isValidToken(token);

        assertEquals(phoneNumber, extractedSubject);
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

    private static UserDetails buildUserDetails(List<String> authorities) {
        return UserDetails.builder()
                .phoneNumber("0798889999")
                .roles(authorities)
                .build();
    }

    private static RefreshTokenRepository repoMock() {
        RefreshTokenRepository mock = mock();
        when(mock.save(any())).then(invocation -> invocation.getArgument(0));
        return mock;
    }
}