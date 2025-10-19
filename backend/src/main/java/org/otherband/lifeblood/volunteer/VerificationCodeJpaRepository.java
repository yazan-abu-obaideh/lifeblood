package org.otherband.lifeblood.volunteer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeJpaRepository extends JpaRepository<PhoneNumberVerificationCodeEntity, Long> {

    Optional<PhoneNumberVerificationCodeEntity> findByPhoneNumberAndVerificationCode(String phoneNumber, String verificationCode);

}
