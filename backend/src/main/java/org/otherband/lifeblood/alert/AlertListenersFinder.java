package org.otherband.lifeblood.alert;

import org.otherband.lifeblood.generated.model.AlertLevel;
import org.otherband.lifeblood.volunteer.VolunteerEntity;
import org.otherband.lifeblood.volunteer.VolunteerJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertListenersFinder {

    private final VolunteerJpaRepository volunteerJpaRepository;

    public AlertListenersFinder(VolunteerJpaRepository volunteerJpaRepository) {
        this.volunteerJpaRepository = volunteerJpaRepository;
    }

    public List<VolunteerEntity> findListeners(AlertEntity alertEntity) {
        AlertLevel alertLevel = alertEntity.getAlertLevel();
        return switch (alertLevel) {
            case ROUTINE, URGENT -> volunteerJpaRepository
                    .findByAlertableHospitalsContainsAndMinimumSeverityGreaterThanEqual(alertEntity.getHospital(),
                            AlertLevelUtils.toLevel(alertLevel));
            case LIFE_OR_DEATH -> volunteerJpaRepository.findAll();
        };
    }

}
