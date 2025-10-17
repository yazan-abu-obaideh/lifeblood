package org.otherband.lifeblood.hospital;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalJpaRepository extends JpaRepository<HospitalEntity, Long> {
    Optional<HospitalEntity> findByUuid(String uuid);
}
