package org.otherband.volunteer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;

import java.util.List;


public record VolunteerRegistrationRequest(
        @NotBlank(message = "please enter your phone number") String phoneNumber,
        @NotEmpty(message = "please choose at least one hospital of interest") List<String> selectedHospitals
) {
}
