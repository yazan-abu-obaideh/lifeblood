package org.otherband.lifeblood.alert;

import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.otherband.lifeblood.ApplicationMapper;
import org.otherband.lifeblood.UserException;
import org.otherband.lifeblood.hospital.HospitalJpaRepository;
import org.otherband.lifeblood.notifications.NotificationChannel;
import org.otherband.lifeblood.notifications.push.PushNotification;
import org.otherband.lifeblood.notifications.push.PushNotificationRepository;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageEntity;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.otherband.lifeblood.volunteer.VolunteerEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RestController
@RequestMapping(AlertController.ALERT_API)
public class AlertController {

    public static final String ALERT_API = "/api/v1/alert";

    private final AlertJpaRepository alertJpaRepository;
    private final AlertListenersFinder alertListenersFinder;
    private final HospitalJpaRepository hospitalJpaRepository;
    private final WhatsAppMessageRepository whatsAppMessageRepository;
    private final PushNotificationRepository pushNotificationRepository;
    private final ApplicationMapper mapper;

    public AlertController(AlertJpaRepository alertJpaRepository, AlertListenersFinder alertListenersFinder,
                           HospitalJpaRepository hospitalJpaRepository, WhatsAppMessageRepository whatsAppMessageRepository, PushNotificationRepository pushNotificationRepository,
                           ApplicationMapper mapper) {
        this.alertJpaRepository = alertJpaRepository;
        this.alertListenersFinder = alertListenersFinder;
        this.hospitalJpaRepository = hospitalJpaRepository;
        this.whatsAppMessageRepository = whatsAppMessageRepository;
        this.pushNotificationRepository = pushNotificationRepository;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertEntity createAlert(@RequestBody @Valid AlertCreationRequest request) {
        AlertEntity alert = mapper.toEntity(request);
        alert.setHospital(hospitalJpaRepository.findByUuid(request.hospitalUuid())
                .orElseThrow(() -> new UserException("Hospital with uuid [%s] does not exist"))
        );
        AlertEntity saved = alertJpaRepository.save(alert);
        List<VolunteerEntity> volunteers = alertListenersFinder.findListeners(alert);
        whatsAppMessageRepository.saveAll(toWhatsAppMessages(request, volunteers));
        pushNotificationRepository.saveAll(toPushNotifications(alert, volunteers));
        return saved;
    }

    private List<PushNotification> toPushNotifications(AlertEntity alert, List<VolunteerEntity> volunteers) {
        return volunteers
                .stream()
                .filter(
                        volunteerEntity ->
                                volunteerEntity.getNotificationChannels().contains(NotificationChannel.PUSH_NOTIFICATIONS.name()))
                .filter(volunteerEntity -> StringUtils.isNotBlank(volunteerEntity.getPushNotificationToken()))
                .map(volunteerEntity ->
                        PushNotification.builder()
                                .pushNotificationType(volunteerEntity.getPushNotificationType())
                                .userToken(volunteerEntity.getPushNotificationToken())
                                .title("%s alert".formatted(alert.getAlertLevel().getDisplayName()))
                                .body(buildPushNotificationBody(alert))
                                .build())
                .toList();
    }

    private static String buildPushNotificationBody(AlertEntity alert) {
        return "Donation request at hospital %s with level %s."
                .formatted(alert.getHospital().getHospitalName(), alert.getAlertLevel().getDisplayName())

                .concat(ofNullable(alert.getDoctorMessage()).filter(StringUtils::isNotBlank).map(" Doctor message: %s"::formatted).orElse(""));
    }

    private static List<WhatsAppMessageEntity> toWhatsAppMessages(AlertCreationRequest request, List<VolunteerEntity> volunteers) {
        return volunteers
                .stream()
                .filter(
                        volunteerEntity ->
                                volunteerEntity.getNotificationChannels().contains(NotificationChannel.WHATSAPP_MESSAGES.name()))
                .map(volunteerEntity ->
                        WhatsAppMessageEntity.builder()
                                .templateName("donation_alert")
                                .phoneNumber(volunteerEntity.getPhoneNumber())
                                .templateVariables(List.of(
                                        request.alertLevel().name(),
                                        request.doctorMessage()
                                ))
                                .build())
                .toList();
    }

}
