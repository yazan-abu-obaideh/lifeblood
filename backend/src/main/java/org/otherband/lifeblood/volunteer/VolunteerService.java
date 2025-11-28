package org.otherband.lifeblood.volunteer;

import org.otherband.lifeblood.ApplicationMapper;
import org.otherband.lifeblood.PhoneNumberUtil;
import org.otherband.lifeblood.TimeService;
import org.otherband.lifeblood.UserException;
import org.otherband.lifeblood.auth.AuthEntity;
import org.otherband.lifeblood.auth.AuthenticationJpaRepository;
import org.otherband.lifeblood.auth.RoleConstants;
import org.otherband.lifeblood.generated.model.NotificationChannel;
import org.otherband.lifeblood.generated.model.PhoneVerificationRequest;
import org.otherband.lifeblood.generated.model.VolunteerRegistrationRequest;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.otherband.lifeblood.hospital.HospitalJpaRepository;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageEntity;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class VolunteerService {
    private final VolunteerJpaRepository volunteerJpaRepository;
    private final HospitalJpaRepository hospitalJpaRepository;
    private final VerificationCodeJpaRepository verificationCodeJpaRepository;
    private final TimeService timeService;
    private final WhatsAppMessageRepository whatsAppMessageRepository;
    private final AuthenticationJpaRepository authenticationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationMapper mapper;

    public VolunteerService(VolunteerJpaRepository volunteerJpaRepository,
                            HospitalJpaRepository hospitalJpaRepository,
                            VerificationCodeJpaRepository verificationCodeJpaRepository,
                            TimeService timeService,
                            WhatsAppMessageRepository whatsAppMessageRepository, AuthenticationJpaRepository authenticationRepository, PasswordEncoder passwordEncoder,
                            ApplicationMapper mapper) {
        this.volunteerJpaRepository = volunteerJpaRepository;
        this.hospitalJpaRepository = hospitalJpaRepository;
        this.verificationCodeJpaRepository = verificationCodeJpaRepository;
        this.timeService = timeService;
        this.whatsAppMessageRepository = whatsAppMessageRepository;
        this.authenticationRepository = authenticationRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    public VolunteerEntity findActiveUserByUuid(String volunteerUuid) {
        Optional<VolunteerEntity> byUuid = volunteerJpaRepository.findByUuid(volunteerUuid);
        if (byUuid.isEmpty()) {
            throw new UserException("No user was found with uuid [%s]".formatted(volunteerUuid));
        }
        VolunteerEntity user = byUuid.get();
        if (!user.isVerifiedPhoneNumber()) {
            throw new UserException("Please verify your phone number before accessing home screen");
        }
        return user;
    }

    @Transactional
    public VolunteerEntity registerVolunteer(VolunteerRegistrationRequest volunteerRequest) {
        String formattedPhoneNumber = formatPhoneNumber(volunteerRequest.getPhoneNumber());
        
        VolunteerEntity entity = mapper.toEntity(volunteerRequest);
        entity.setUuid(UUID.randomUUID().toString());
        entity.setPhoneNumber(formattedPhoneNumber);
        entity.setAlertableHospitals(mapToHospitals(volunteerRequest.getSelectedHospitals()));
        entity.setNotificationChannels(Arrays.stream(NotificationChannel.values()).map(Enum::name).toList());
        entity.setMinimumSeverity(0);

        PhoneNumberVerificationCodeEntity verificationCode = new PhoneNumberVerificationCodeEntity();
        verificationCode.setPhoneNumber(formattedPhoneNumber);
        verificationCode.setVerificationCode(UUID.randomUUID().toString());

        verificationCodeJpaRepository.save(verificationCode);

        whatsAppMessageRepository.save(WhatsAppMessageEntity.builder()
                        .templateName("verification_code")
                        .phoneNumber(formattedPhoneNumber)
                        .templateVariables(List.of(verificationCode.getVerificationCode()))
                .build());

        authenticationRepository.save(AuthEntity.builder()
                        .phoneNumber(entity.getPhoneNumber())
                        .userUuid(entity.getUuid())
                        .hashedPassword(passwordEncoder.encode(volunteerRequest.getPassword()))
                        .roles(Set.of(RoleConstants.VOLUNTEER_ROLE))
                .build());

        return volunteerJpaRepository.save(entity);
    }

    @Transactional
    public void verifyPhoneNumber(PhoneVerificationRequest request) {
        String formattedPhoneNumber = formatPhoneNumber(request.getPhoneNumber());
        Optional<PhoneNumberVerificationCodeEntity> result = verificationCodeJpaRepository.findByPhoneNumberAndVerificationCode(
                formattedPhoneNumber, request.getVerificationCode()
        );

        if (result.isEmpty()) {
            throw new UserException(
                    "Verification code '%s' for phone number '%s' invalid or expired"
                            .formatted(request.getVerificationCode(), formattedPhoneNumber)
            );
        }

        PhoneNumberVerificationCodeEntity verificationCode = result.get();
        if (verificationCode.getCreationDate().isBefore(timeService.now().minusMinutes(10))) {
            throw new UserException("Verification code has expired. Please request a new one.");
        }

        VolunteerEntity volunteer = volunteerJpaRepository.findByPhoneNumber(formattedPhoneNumber)
                .orElseThrow(() -> new AssertionError(
                        "This should never happen: the verification code was found, but not the volunteer"));

        volunteer.setVerifiedPhoneNumber(true);
        volunteerJpaRepository.save(volunteer);
    }

    private List<HospitalEntity> mapToHospitals(List<String> hospitalUuids) {
        return hospitalUuids.stream()
                .map(hospitalUuid -> {
                    Optional<HospitalEntity> hospital = hospitalJpaRepository.findByUuid(hospitalUuid);
                    if (hospital.isEmpty()) {
                        throw new IllegalArgumentException("Hospital with uuid [%s] does not exist".formatted(hospitalUuid));
                    }
                    return hospital;
                })
                .map(Optional::get)
                .toList();
    }

    private static String formatPhoneNumber(String phoneNumber) {
        return PhoneNumberUtil.INSTANCE.formatPhoneNumber(phoneNumber);
    }
}
