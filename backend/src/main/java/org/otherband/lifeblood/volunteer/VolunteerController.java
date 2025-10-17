package org.otherband.lifeblood.volunteer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(VolunteerController.VOLUNTEER_API)
public class VolunteerController {

    public static final String VOLUNTEER_API = "/api/v1/volunteer";

    private final VolunteerService volunteerService;

    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VolunteerEntity registerVolunteer(@RequestBody @Valid VolunteerRegistrationRequest volunteerRequest) {
        return volunteerService.registerVolunteer(volunteerRequest);
    }

    @PostMapping("/verify-phone-number")
    public void verifyPhoneNumber(@RequestBody PhoneVerificationRequest request) {
        volunteerService.verifyPhoneNumber(request);
    }

}
