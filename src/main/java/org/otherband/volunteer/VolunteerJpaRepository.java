package org.otherband.volunteer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolunteerJpaRepository extends JpaRepository<VolunteerEntity, Long> {
}
