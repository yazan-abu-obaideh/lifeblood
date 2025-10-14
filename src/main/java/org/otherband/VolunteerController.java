package org.otherband;

import jakarta.validation.Valid;
import org.otherband.volunteer.VolunteerEntity;
import org.otherband.volunteer.VolunteerJpaRepository;
import org.otherband.volunteer.VolunteerRegistrationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(VolunteerController.VOLUNTEER_API)
public class VolunteerController {

    public static final String VOLUNTEER_API = "/api/v1/volunteer";
    private final VolunteerJpaRepository volunteerJpaRepository;
    private final ApplicationMapper mapper;

    public VolunteerController(VolunteerJpaRepository volunteerJpaRepository, ApplicationMapper mapper) {
        this.volunteerJpaRepository = volunteerJpaRepository;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VolunteerEntity createVolunteer(@RequestBody @Valid VolunteerRegistrationRequest volunteerRequest) {
        VolunteerEntity entity = mapper.toEntity(volunteerRequest);
        entity.setUuid(UUID.randomUUID().toString());
        return volunteerJpaRepository.save(entity);
    }

}
