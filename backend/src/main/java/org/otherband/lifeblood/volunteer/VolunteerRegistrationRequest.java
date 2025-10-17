package org.otherband.lifeblood.volunteer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;


public record VolunteerRegistrationRequest(
        @NotBlank(message = "please enter your phone number") String phoneNumber,
        @NotEmpty(message = "please choose at least one hospital of interest") List<String> selectedHospitals
) {
}
