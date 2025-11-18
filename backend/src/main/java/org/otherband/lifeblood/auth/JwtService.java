package org.otherband.lifeblood.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.otherband.lifeblood.TimeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtService {

    private final String secretKey;
    private final Duration tokenExpiration;
    private final TimeService timeService;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtService(@Value("${jwt.secret.key}") String secretKey,
                      @Value("${jwt.token.expiration.minutes}") int tokenExpiration,
                      TimeService timeService,
                      RefreshTokenRepository refreshTokenRepository) {
        this.secretKey = secretKey;
        this.tokenExpiration = Duration.ofMinutes(tokenExpiration);
        this.timeService = timeService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateRefreshToken(UserDetails userDetails) {
        LocalDateTime now = timeService.now();
        LocalDateTime expirationDate = now.plus(Duration.of(21, ChronoUnit.DAYS));

        String refreshToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(toDate(now))
                .expiration(toDate(expirationDate))
                .signWith(getSigningKey())
                .claim("refreshToken", true)
                .compact();

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUsername(userDetails.getUsername());
        entity.setTokenHash(String.valueOf(refreshToken.hashCode()));
        refreshTokenRepository.save(entity);

        return refreshToken;
    }

    public boolean isValidRefreshToken(String username, String refreshToken) {
        Jws<Claims> claimsJws = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(refreshToken);
        Boolean isRefreshToken = Optional.of(claimsJws).map(Jwt::getPayload)
                .filter(claims -> username.equals(claims.getSubject()))
                .map(claims -> claims.get("refreshToken"))
                .filter(object -> object instanceof Boolean)
                .map(object -> (Boolean) object)
                .orElse(false);
        return isRefreshToken && hasNotExpired(refreshToken);

    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        claims.put("roles", roles);

        return generateToken(claims, userDetails.getUsername());
    }

    public String generateToken(Map<String, Object> extraClaims, String username) {
        LocalDateTime now = timeService.now();
        LocalDateTime expiration = now.plus(tokenExpiration);
        Date nowDate = toDate(now);
        Date expirationDate = toDate(expiration);

        return Jwts.builder().claims(extraClaims).subject(username).issuedAt(nowDate).expiration(expirationDate).signWith(getSigningKey()).compact();
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return hasNotExpired(token);
        } catch (Exception e) {
            log.error("Exception while validating token {}", token, e);
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Date toDate(LocalDateTime now) {
        return Date.from(now.atZone(timeService.getZoneId()).toInstant());
    }

    private  <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    private boolean hasNotExpired(String token) {
        Date expiration = extractExpiration(token);
        LocalDateTime expirationTime = LocalDateTime.ofInstant(expiration.toInstant(), timeService.getZoneId());
        return !expirationTime.isBefore(timeService.now());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}