package org.otherband.alert;

import jakarta.validation.Valid;
import org.otherband.ApplicationMapper;
import org.otherband.UserException;
import org.otherband.hospital.HospitalJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AlertController.ALERT_API)
public class AlertController {

    public static final String ALERT_API = "/api/v1/alert";

    private final AlertJpaRepository alertJpaRepository;
    private final HospitalJpaRepository hospitalJpaRepository;
    private final ApplicationMapper mapper;

    public AlertController(AlertJpaRepository alertJpaRepository,
                           HospitalJpaRepository hospitalJpaRepository,
                           ApplicationMapper mapper) {
        this.alertJpaRepository = alertJpaRepository;
        this.hospitalJpaRepository = hospitalJpaRepository;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertEntity createAlert(@RequestBody @Valid AlertCreationRequest request) {
        AlertEntity alert = mapper.toEntity(request);
        alert.setHospital(hospitalJpaRepository.findByUuid(request.hospitalUuid())
                .orElseThrow(() -> new UserException("Hospital with uuid [%s] does not exist"))
        );
        return alertJpaRepository.save(alert);
    }

}
