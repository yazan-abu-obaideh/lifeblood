package org.otherband.lifeblood.volunteer;

import org.otherband.lifeblood.ApplicationMapper;
import org.otherband.lifeblood.TimeService;
import org.otherband.lifeblood.UserException;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.otherband.lifeblood.hospital.HospitalJpaRepository;
import org.otherband.lifeblood.notifications.NotificationChannel;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageEntity;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VolunteerService {
    private final VolunteerJpaRepository volunteerJpaRepository;
    private final HospitalJpaRepository hospitalJpaRepository;
    private final VerificationCodeJpaRepository verificationCodeJpaRepository;
    private final TimeService timeService;
    private final WhatsAppMessageRepository whatsAppMessageRepository;
    private final ApplicationMapper mapper;

    public VolunteerService(VolunteerJpaRepository volunteerJpaRepository,
                            HospitalJpaRepository hospitalJpaRepository,
                            VerificationCodeJpaRepository verificationCodeJpaRepository,
                            TimeService timeService,
                            WhatsAppMessageRepository whatsAppMessageRepository,
                            ApplicationMapper mapper) {
        this.volunteerJpaRepository = volunteerJpaRepository;
        this.hospitalJpaRepository = hospitalJpaRepository;
        this.verificationCodeJpaRepository = verificationCodeJpaRepository;
        this.timeService = timeService;
        this.whatsAppMessageRepository = whatsAppMessageRepository;
        this.mapper = mapper;
    }

    @Transactional
    public VolunteerEntity registerVolunteer(VolunteerRegistrationRequest volunteerRequest) {
        VolunteerEntity entity = mapper.toEntity(volunteerRequest);
        entity.setUuid(UUID.randomUUID().toString());
        entity.setAlertableHospitals(mapToHospitals(volunteerRequest.selectedHospitals()));
        entity.setNotificationChannels(Arrays.stream(NotificationChannel.values()).map(Enum::name).toList());
        entity.setMinimumSeverity(0);

        PhoneNumberVerificationCodeEntity verificationCode = new PhoneNumberVerificationCodeEntity();
        verificationCode.setPhoneNumber(volunteerRequest.phoneNumber());
        verificationCode.setVerificationCode(UUID.randomUUID().toString());

        verificationCodeJpaRepository.save(verificationCode);

        whatsAppMessageRepository.save(WhatsAppMessageEntity.builder()
                        .templateName("verification_code")
                        .phoneNumber(volunteerRequest.phoneNumber())
                        .templateVariables(List.of(verificationCode.getVerificationCode()))
                .build());

        return volunteerJpaRepository.save(entity);
    }

    @Transactional
    public void verifyPhoneNumber(PhoneVerificationRequest request) {
        Optional<PhoneNumberVerificationCodeEntity> result = verificationCodeJpaRepository.findByPhoneNumberAndVerificationCode(
                request.phoneNumber(), request.verificationCode()
        );

        if (result.isEmpty()) {
            throw new UserException(
                    "Verification code '%s' for phone number '%s' invalid or expired"
                            .formatted(request.verificationCode(), request.phoneNumber())
            );
        }

        PhoneNumberVerificationCodeEntity verificationCode = result.get();
        if (verificationCode.getCreationDate().isBefore(timeService.now().minusMinutes(10))) {
            throw new UserException("Verification code has expired. Please request a new one.");
        }

        VolunteerEntity volunteer = volunteerJpaRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new AssertionError("This should never happen."));

        volunteer.setVerifiedPhoneNumber(true);
        volunteerJpaRepository.save(volunteer);
    }

    private List<HospitalEntity> mapToHospitals(List<String> hospitalUuids) {
        return hospitalUuids.stream()
                .map(hospitalUuid -> {
                    Optional<HospitalEntity> hospital = hospitalJpaRepository.findByUuid(hospitalUuid);
                    if (hospital.isEmpty()) {
                        throw new IllegalArgumentException("Hospital with uuid [%s] does not exist");
                    }
                    return hospital;
                })
                .map(Optional::get)
                .toList();
    }

}
