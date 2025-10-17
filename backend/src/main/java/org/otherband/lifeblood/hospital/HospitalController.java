package org.otherband.lifeblood.hospital;

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

    public HospitalController(HospitalJpaRepository hospitalJpaRepository) {
        this.hospitalJpaRepository = hospitalJpaRepository;
    }

    @GetMapping
    public List<HospitalEntity> getAll() {
        return hospitalJpaRepository.findAll();
    }

}
