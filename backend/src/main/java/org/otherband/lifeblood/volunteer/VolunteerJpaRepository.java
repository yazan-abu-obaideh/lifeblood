package org.otherband.lifeblood.volunteer;

import org.otherband.lifeblood.hospital.HospitalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerJpaRepository extends JpaRepository<VolunteerEntity, Long> {
    Optional<VolunteerEntity> findByPhoneNumber(String phoneNumber);
    Optional<VolunteerEntity> findByUuid(String uuid);
    List<VolunteerEntity> findByAlertableHospitalsContainsAndMinimumSeverityGreaterThanEqual(HospitalEntity hospitalEntity, int minimumSeverity);
}
