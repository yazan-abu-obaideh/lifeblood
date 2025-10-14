package org.otherband.volunteer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class VolunteerRegistrationRequest {
    @NotBlank(message = "please enter your phone number")
    private String phoneNumber;
    @NotEmpty(message = "Please choose at least one hospital of interest")
    private List<String> selectedHospitals;
}
