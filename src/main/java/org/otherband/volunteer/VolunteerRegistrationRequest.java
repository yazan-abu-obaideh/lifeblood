package org.otherband.volunteer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VolunteerRegistrationRequest {
    @NotBlank(message = "please enter your phone number")
    private String phoneNumber;
}
