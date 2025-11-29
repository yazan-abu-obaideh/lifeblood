package org.otherband.lifeblood.volunteer;

import org.otherband.lifeblood.ApplicationMapper;
import org.otherband.lifeblood.auth.RoleConstants;
import org.otherband.lifeblood.generated.model.PhoneVerificationRequest;
import org.otherband.lifeblood.generated.model.UpdateVolunteerSettingsRequest;
import org.otherband.lifeblood.generated.model.VolunteerRegistrationRequest;
import org.otherband.lifeblood.generated.model.VolunteerResponse;
import org.otherband.lifeblood.validations.SimpleValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
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
    @PreAuthorize("#uuid == authentication.principal['user_uuid']")
    public VolunteerResponse getData(@P("uuid") @PathVariable("uuid") String uuid) {
        return mapper.toResponse(volunteerService.findActiveUserByUuid(uuid));
    }

    @PatchMapping("/{uuid}/update-settings")
    @PreAuthorize("#uuid == authentication.principal['user_uuid']")
    public VolunteerResponse updateSettings(@P("uuid") @PathVariable("uuid") String uuid,
                                            @RequestBody UpdateVolunteerSettingsRequest updateVolunteerSettingsRequest) {
        return mapper.toResponse(volunteerService.updateUserSettings(uuid, updateVolunteerSettingsRequest));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(RoleConstants.ALLOW_ALL)
    public VolunteerResponse registerVolunteer(@RequestBody VolunteerRegistrationRequest volunteerRequest) {
        SimpleValidator.INSTANCE.validate(volunteerRequest);
        return mapper.toResponse(volunteerService.registerVolunteer(volunteerRequest));
    }

    @PostMapping("/verify-phone-number")
    @PreAuthorize(RoleConstants.ALLOW_ALL)
    public void verifyPhoneNumber(@RequestBody PhoneVerificationRequest request) {
        volunteerService.verifyPhoneNumber(request);
    }

}
