package org.otherband.lifeblood.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticationJpaRepository extends JpaRepository<AuthEntity, Long> {
    Optional<AuthEntity> findAuthEntityByPhoneNumber(String phoneNumber);
}
