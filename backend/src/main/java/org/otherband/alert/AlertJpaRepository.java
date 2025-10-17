package org.otherband.alert;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertJpaRepository extends JpaRepository<AlertEntity, Long> {
}
