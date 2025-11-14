package org.otherband.lifeblood.auth;

import io.jsonwebtoken.Claims;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtService {

    private final String secretKey;
    private final Duration tokenExpiration;
    private final TimeService timeService;

    public JwtService(@Value("${jwt.secret.key}") String secretKey,
                      @Value("${jwt.token.expiration.minutes}") int tokenExpiration,
                      TimeService timeService) {
        this.secretKey = secretKey;
        this.tokenExpiration = Duration.ofMinutes(tokenExpiration);
        this.timeService = timeService;
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
        Date nowDate = Date.from(now.atZone(timeService.getZoneId()).toInstant());
        Date expirationDate = Date.from(expiration.atZone(timeService.getZoneId()).toInstant());

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