package org.otherband.lifeblood.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.otherband.lifeblood.alert.AlertLevelUtils;

public class AlertSeverityValidator implements ConstraintValidator<ValidAlertSeverity, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value >= AlertLevelUtils.minimumSeverity() &&
                value <= AlertLevelUtils.maximumSeverity();
    }
}
