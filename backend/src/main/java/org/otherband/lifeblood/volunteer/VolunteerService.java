package org.otherband.lifeblood.volunteer;

import org.otherband.lifeblood.ApplicationMapper;
import org.otherband.lifeblood.TimeService;
import org.otherband.lifeblood.UserException;
import org.otherband.lifeblood.hospital.HospitalJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class VolunteerService {
    private final VolunteerJpaRepository volunteerJpaRepository;
    private final HospitalJpaRepository hospitalJpaRepository;
    private final VerificationCodeJpaRepository verificationCodeJpaRepository;
    private final TimeService timeService;
    private final VerificationCodeSender verificationCodeSender;
    private final ApplicationMapper mapper;

    public VolunteerService(VolunteerJpaRepository volunteerJpaRepository,
                            HospitalJpaRepository hospitalJpaRepository,
                            VerificationCodeJpaRepository verificationCodeJpaRepository,
                            TimeService timeService, VerificationCodeSender verificationCodeSender,
                            ApplicationMapper mapper) {
        this.volunteerJpaRepository = volunteerJpaRepository;
        this.hospitalJpaRepository = hospitalJpaRepository;
        this.verificationCodeJpaRepository = verificationCodeJpaRepository;
        this.timeService = timeService;
        this.verificationCodeSender = verificationCodeSender;
        this.mapper = mapper;
    }

    @Transactional
    public VolunteerEntity registerVolunteer(VolunteerRegistrationRequest volunteerRequest) {
        VolunteerEntity entity = mapper.toEntity(volunteerRequest);
        entity.setUuid(UUID.randomUUID().toString());
        entity.setAlertableHospitals(volunteerRequest.selectedHospitals().stream()
                .map(hospitalJpaRepository::findByUuid)
                .filter(hospital -> {
                    if (hospital.isEmpty()) {
                        throw new IllegalArgumentException("Hospital with uuid [%s] does not exist");
                    }
                    return true;
                })
                .map(Optional::get)
                .toList());

        VerificationCodeEntity verificationCode = new VerificationCodeEntity();
        verificationCode.setPhoneNumber(volunteerRequest.phoneNumber());
        verificationCode.setVerificationCode(UUID.randomUUID().toString());

        verificationCodeJpaRepository.save(verificationCode);
        verificationCodeSender.send(verificationCode);
        return volunteerJpaRepository.save(entity);
    }

    public void verifyPhoneNumber(PhoneVerificationRequest request) {
        Optional<VerificationCodeEntity> result = verificationCodeJpaRepository.findByPhoneNumberAndVerificationCode(
                request.phoneNumber(), request.verificationCode()
        );

        if (result.isEmpty()) {
            throw new UserException(
                    "Verification code '%s' for phone number '%s' invalid or expired"
                            .formatted(request.verificationCode(), request.phoneNumber())
            );
        }

        VerificationCodeEntity verificationCode = result.get();
        if (verificationCode.getCreationDate().isBefore(timeService.now().minusMinutes(10))) {
            throw new UserException("Verification code has expired. Please request a new one.");
        }

        VolunteerEntity volunteer = volunteerJpaRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new AssertionError("This should never happen."));

        volunteer.setVerifiedPhoneNumber(true);
        volunteerJpaRepository.save(volunteer);
    }

}
