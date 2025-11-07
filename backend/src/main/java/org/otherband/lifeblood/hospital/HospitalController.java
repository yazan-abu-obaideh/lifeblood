package org.otherband.lifeblood.hospital;

import org.otherband.lifeblood.ApplicationMapper;
import org.otherband.lifeblood.generated.model.HospitalResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.otherband.lifeblood.hospital.HospitalController.HOSPITAL_API;

@RestController
@RequestMapping(HOSPITAL_API)
public class HospitalController {
    public static final String HOSPITAL_API = "/api/v1/hospital";


    private final HospitalJpaRepository hospitalJpaRepository;
    private final ApplicationMapper mapper;

    public HospitalController(HospitalJpaRepository hospitalJpaRepository, ApplicationMapper mapper) {
        this.hospitalJpaRepository = hospitalJpaRepository;
        this.mapper = mapper;
    }

    @GetMapping
    public List<HospitalResponse> getAll() {
        return hospitalJpaRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

}
