package org.otherband.dev;

import lombok.extern.slf4j.Slf4j;
import org.otherband.ProfileConstants;
import org.otherband.volunteer.VerificationCodeSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile(ProfileConstants.DEVELOPMENT)
@Configuration
@Slf4j
public class DevConfig {

    public DevConfig() {
        log.warn("Running in dev mode. Development beans will be created.");
    }

    @Bean
    public VerificationCodeSender verificationCodeSender() {
        return verificationCode -> {
            log.info("Verification code: [{}]", verificationCode);
        };
    }

}
