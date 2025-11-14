package org.otherband.lifeblood.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

    @PostMapping
    @RequestMapping("/login")
    @PreAuthorize("permitAll()")
    public String login() {
        return "";
    }

    @PostMapping
    @RequestMapping("/refresh")
    @PreAuthorize("permitAll()")
    public String refresh() {
        return "";
    }

}
