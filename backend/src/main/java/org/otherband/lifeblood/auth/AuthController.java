package org.otherband.lifeblood.auth;

import org.otherband.lifeblood.UserAuthException;
import org.otherband.lifeblood.generated.model.LoginRequest;
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

@RestController
@RequestMapping(AuthController.AUTH_API)
public class AuthController {

    public static final String AUTH_API = "/api/v1/auth";

    private final AuthenticationJpaRepository authenticationJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationJpaRepository authenticationJpaRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationJpaRepository = authenticationJpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping
    @RequestMapping("/login")
    @PreAuthorize(RoleConstants.ALLOW_ALL)
    public String login(@RequestBody LoginRequest loginRequest) {
        AuthEntity auth =
                authenticationJpaRepository.findAuthEntityByUsername(loginRequest.getUsername())
                .filter(authEntity -> passwordsMatch(authEntity.getHashedPassword(), loginRequest.getPassword()))
                .orElseThrow(() -> new UserAuthException("Username and password combination not found"));
        return jwtService.generateToken(buildUser(auth));
    }

    @PostMapping
    @RequestMapping("/refresh")
    @PreAuthorize(RoleConstants.ALLOW_ALL)
    public String refresh() {
        return "";
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
