package org.otherband.lifeblood.alert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.otherband.lifeblood.generated.model.AlertLevel;

public record AlertCreationRequest(
        @NotBlank(message = "please enter a hospital uuid") String hospitalUuid,
        @NotNull(message = "Alert level must be specified") AlertLevel alertLevel,
        String doctorMessage
) {
}
