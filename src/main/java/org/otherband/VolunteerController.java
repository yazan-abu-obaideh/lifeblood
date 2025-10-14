package org.otherband;

import jakarta.validation.Valid;
import org.otherband.hospital.HospitalJpaRepository;
import org.otherband.volunteer.VolunteerEntity;
import org.otherband.volunteer.VolunteerJpaRepository;
import org.otherband.volunteer.VolunteerRegistrationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(VolunteerController.VOLUNTEER_API)
public class VolunteerController {

    public static final String VOLUNTEER_API = "/api/v1/volunteer";
    private final VolunteerJpaRepository volunteerJpaRepository;
    private final HospitalJpaRepository hospitalJpaRepository;
    private final ApplicationMapper mapper;

    public VolunteerController(VolunteerJpaRepository volunteerJpaRepository, HospitalJpaRepository hospitalJpaRepository, ApplicationMapper mapper) {
        this.volunteerJpaRepository = volunteerJpaRepository;
        this.hospitalJpaRepository = hospitalJpaRepository;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VolunteerEntity registerVolunteer(@RequestBody @Valid VolunteerRegistrationRequest volunteerRequest) {
        VolunteerEntity entity = mapper.toEntity(volunteerRequest);
        entity.setUuid(UUID.randomUUID().toString());
        entity.setAlertableHospitals(volunteerRequest.getSelectedHospitals().stream()
                .map(hospitalJpaRepository::findByUuid)
                .filter(hospital -> {
                    if (hospital.isEmpty()) {
                        throw new IllegalArgumentException("Hospital with uuid [%s] not found");
                    }
                    return true;
                })
                .map(Optional::get)
                .toList());
        return volunteerJpaRepository.save(entity);
    }

}
