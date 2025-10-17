package org.otherband.lifeblood.volunteer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VolunteerJpaRepository extends JpaRepository<VolunteerEntity, Long> {
    Optional<VolunteerEntity> findByPhoneNumber(String phoneNumber);
}
