package org.otherband.lifeblood.auth;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.AssertionFailure;
import org.otherband.lifeblood.UserAuthException;
import org.otherband.lifeblood.generated.model.LoginRequest;
import org.otherband.lifeblood.generated.model.LoginResponse;
import org.otherband.lifeblood.generated.model.RefreshTokenRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(AuthController.AUTH_API)
public class AuthController {

    public static final String AUTH_API = "/api/v1/auth";

    private final AuthenticationJpaRepository authenticationJpaRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationJpaRepository authenticationJpaRepository,
                          RefreshTokenRepository refreshTokenRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.authenticationJpaRepository = authenticationJpaRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping
    @RequestMapping("/login")
    @PreAuthorize(RoleConstants.ALLOW_ALL)
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        AuthEntity auth =
                authenticationJpaRepository.findAuthEntityByUsername(loginRequest.getUsername())
                .filter(authEntity -> passwordsMatch(authEntity.getHashedPassword(), loginRequest.getPassword()))
                .orElseThrow(() -> new UserAuthException("Username and password combination not found"));

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setRefreshToken(jwtService.generateRefreshToken(buildUser(auth)));
        loginResponse.setPhoneNumber(auth.getUsername());
        loginResponse.setUserUuid(auth.getUserUuid());
        return loginResponse;
    }

    @PostMapping
    @RequestMapping("/refresh")
    @PreAuthorize(RoleConstants.ALLOW_ALL)
    public String refresh(@RequestBody RefreshTokenRequest request) {
        if (!jwtService.isValidRefreshToken(request.getUsername(), request.getRefreshToken())) {
            throw new UserAuthException("Invalid or expired refresh token");
        }
        refreshTokenRepository.findRefreshTokenEntityByUsername(request.getUsername())
                .stream()
                .filter(refreshTokenEntity ->
                        refreshTokenEntity.getTokenHash().equals(String.valueOf(request.getRefreshToken().hashCode())))
                .findFirst()
                .orElseThrow(() ->
                        new UserAuthException("Refresh token was considered valid, but it does not exist in the repository. Revoked or forged."));
        return authenticationJpaRepository.findAuthEntityByUsername(request.getUsername())
                .map(authEntity -> jwtService.generateToken(buildUser(authEntity)))
                .orElseThrow(() -> new AssertionFailure("Disastrous: refresh token was considered valid, but user was not found"));
    }

    private static UserDetails buildUser(AuthEntity auth) {
        return User.builder()
                .username(auth.getUsername())
                .password(auth.getHashedPassword())
                .authorities(auth.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()))
                .build();
    }

    private boolean passwordsMatch(String actualHashed, String submitted) {
        return passwordEncoder.matches(submitted, actualHashed);
    }

}
