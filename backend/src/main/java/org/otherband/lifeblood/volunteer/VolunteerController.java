package org.otherband.lifeblood.volunteer;

import jakarta.validation.Valid;
import org.otherband.lifeblood.ApplicationMapper;
import org.otherband.lifeblood.generated.model.VolunteerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(VolunteerController.VOLUNTEER_API)
public class VolunteerController {

    public static final String VOLUNTEER_API = "/api/v1/volunteer";

    private final VolunteerService volunteerService;
    private final ApplicationMapper mapper;

    public VolunteerController(VolunteerService volunteerService, ApplicationMapper mapper) {
        this.volunteerService = volunteerService;
        this.mapper = mapper;
    }

    @GetMapping("/{uuid}")
    public VolunteerResponse getData(@PathVariable("uuid") String uuid) {
        return mapper.toResponse(volunteerService.findActiveUserByUuid(uuid));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VolunteerResponse registerVolunteer(@RequestBody @Valid VolunteerRegistrationRequest volunteerRequest) {
        return mapper.toResponse(volunteerService.registerVolunteer(volunteerRequest));
    }

    @PostMapping("/verify-phone-number")
    public void verifyPhoneNumber(@RequestBody PhoneVerificationRequest request) {
        volunteerService.verifyPhoneNumber(request);
    }

}
