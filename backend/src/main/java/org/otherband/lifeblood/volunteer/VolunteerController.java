package org.otherband.lifeblood.volunteer;

import org.otherband.lifeblood.ApplicationMapper;
import org.otherband.lifeblood.generated.model.PhoneVerificationRequest;
import org.otherband.lifeblood.generated.model.VolunteerRegistrationRequest;
import org.otherband.lifeblood.generated.model.VolunteerResponse;
import org.otherband.lifeblood.validations.SimpleValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('volunteer')")
    public VolunteerResponse getData(@PathVariable("uuid") String uuid) {
        return mapper.toResponse(volunteerService.findActiveUserByUuid(uuid));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("permitAll()")
    public VolunteerResponse registerVolunteer(@RequestBody VolunteerRegistrationRequest volunteerRequest) {
        SimpleValidator.INSTANCE.validate(volunteerRequest);
        return mapper.toResponse(volunteerService.registerVolunteer(volunteerRequest));
    }

    @PostMapping("/verify-phone-number")
    @PreAuthorize("hasRole('volunteer')")
    public void verifyPhoneNumber(@RequestBody PhoneVerificationRequest request) {
        volunteerService.verifyPhoneNumber(request);
    }

}
