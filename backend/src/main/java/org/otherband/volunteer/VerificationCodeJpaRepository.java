package org.otherband.volunteer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeJpaRepository extends JpaRepository<VerificationCodeEntity, Long> {

    Optional<VerificationCodeEntity> findByPhoneNumberAndVerificationCode(String phoneNumber, String verificationCode);

}
