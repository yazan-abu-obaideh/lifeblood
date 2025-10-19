package org.otherband.lifeblood.volunteer;

import org.otherband.lifeblood.validations.ValidAlertSeverity;

import java.util.List;

public record VolunteerUpdateSettingsRequest(
        @ValidAlertSeverity int minimumAlertSeverity,
        List<String> alertableHospitals
) {
}
