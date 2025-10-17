package org.otherband.alert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlertCreationRequest(
        @NotBlank(message = "please enter a hospital uuid") String hospitalUuid,
        @NotNull AlertLevel alertLevel,
        String doctorMessage
) {
}
