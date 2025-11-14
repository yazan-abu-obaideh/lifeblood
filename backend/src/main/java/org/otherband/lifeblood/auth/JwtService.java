package org.otherband.lifeblood.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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

    public JwtService(@Value("${jwt.secret.key}") String secretKey,
                      @Value("${jwt.token.expiration.minutes}") int tokenExpiration) {
        this.secretKey = secretKey;
        this.tokenExpiration = Duration.ofMinutes(tokenExpiration);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        claims.put("roles", roles);

        return generateToken(claims, userDetails.getUsername());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return hasNotExpired(token);
        } catch (Exception e) {
            log.error("Exception while validating token {}", token, e);
            return false;
        }
    }

    public String generateToken(Map<String, Object> extraClaims, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + tokenExpiration.toMillis());
        return Jwts.builder().claims(extraClaims).subject(username).issuedAt(now).expiration(expiration).signWith(getSigningKey())  // Auto-detects HS256 from key
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    private boolean hasNotExpired(String token) {
        return !extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && hasNotExpired(token);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}