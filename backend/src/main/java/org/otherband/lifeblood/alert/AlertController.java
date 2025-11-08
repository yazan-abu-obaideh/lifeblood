package org.otherband.lifeblood.alert;

import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.otherband.lifeblood.ApplicationMapper;
import org.otherband.lifeblood.UserException;
import org.otherband.lifeblood.generated.model.AlertCreationRequest;
import org.otherband.lifeblood.generated.model.AlertResponse;
import org.otherband.lifeblood.generated.model.NotificationChannel;
import org.otherband.lifeblood.generated.model.PageAlertResponse;
import org.otherband.lifeblood.hospital.HospitalJpaRepository;
import org.otherband.lifeblood.notifications.push.PushNotification;
import org.otherband.lifeblood.notifications.push.PushNotificationRepository;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageEntity;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.otherband.lifeblood.volunteer.VolunteerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Optional.ofNullable;

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

    public AlertController(AlertJpaRepository alertJpaRepository,
                           AlertListenersFinder alertListenersFinder,
                           HospitalJpaRepository hospitalJpaRepository,
                           WhatsAppMessageRepository whatsAppMessageRepository,
                           PushNotificationRepository pushNotificationRepository,
                           ApplicationMapper mapper) {
        this.alertJpaRepository = alertJpaRepository;
        this.alertListenersFinder = alertListenersFinder;
        this.hospitalJpaRepository = hospitalJpaRepository;
        this.whatsAppMessageRepository = whatsAppMessageRepository;
        this.pushNotificationRepository = pushNotificationRepository;
        this.mapper = mapper;
    }

    @GetMapping
    public PageAlertResponse getAlerts(@RequestParam(required = false, defaultValue = "10", name = "pageSize") int pageSize,
                                       @RequestParam(required = false, defaultValue = "0", name = "pageNumber") int pageNumber,
                                       @RequestParam(required = false, name = "activeOnly") boolean activeOnly) {
        final Page<AlertEntity> result;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        if (activeOnly) {
            result = alertJpaRepository.findAllByFulfilmentDateIsNullOrderByCreationDateDesc(pageable);
        } else {
            result = alertJpaRepository.findAllByOrderByCreationDateDesc(pageable);
        }
        return mapper.toResponse(result);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponse createAlert(@RequestBody @Valid AlertCreationRequest request) {
        AlertEntity alert = mapper.toEntity(request);
        alert.setHospital(hospitalJpaRepository.findByUuid(request.getHospitalUuid())
                .orElseThrow(() -> new UserException("Hospital with uuid [%s] does not exist"))
        );
        AlertEntity saved = alertJpaRepository.save(alert);
        List<VolunteerEntity> volunteers = alertListenersFinder.findListeners(alert);
        whatsAppMessageRepository.saveAll(toWhatsAppMessages(request, volunteers));
        pushNotificationRepository.saveAll(toPushNotifications(alert, volunteers));
        return mapper.toResponse(saved);
    }

    private List<PushNotification> toPushNotifications(AlertEntity alert, List<VolunteerEntity> volunteers) {
        return volunteers
                .stream()
                .filter(
                        volunteerEntity ->
                                receivesNotification(volunteerEntity, NotificationChannel.PUSH_NOTIFICATIONS))
                .filter(volunteerEntity -> StringUtils.isNotBlank(volunteerEntity.getPushNotificationToken()))
                .map(volunteerEntity -> toPushNotification(alert, volunteerEntity))
                .toList();
    }

    private static PushNotification toPushNotification(AlertEntity alert, VolunteerEntity volunteerEntity) {
        return PushNotification.builder()
                .pushNotificationType(volunteerEntity.getPushNotificationType())
                .userToken(volunteerEntity.getPushNotificationToken())
                .title("%s alert".formatted(AlertLevelUtils.toDisplayName(alert.getAlertLevel())))
                .body(buildPushNotificationBody(alert))
                .build();
    }

    private static String buildPushNotificationBody(AlertEntity alert) {
        String hospitalName = alert.getHospital().getHospitalName();
        String alertLevel = AlertLevelUtils.toDisplayName(alert.getAlertLevel());
        return "Donation request at hospital %s with level %s."
                .formatted(hospitalName, alertLevel)
                .concat(doctorMessageOrEmpty(alert));
    }

    private List<WhatsAppMessageEntity> toWhatsAppMessages(AlertCreationRequest request, List<VolunteerEntity> volunteers) {
        return volunteers
                .stream()
                .filter(volunteerEntity ->
                        receivesNotification(volunteerEntity, NotificationChannel.WHATSAPP_MESSAGES))
                .map(volunteerEntity -> toWhatsAppMessage(request, volunteerEntity))
                .toList();
    }

    private static WhatsAppMessageEntity toWhatsAppMessage(AlertCreationRequest request, VolunteerEntity volunteerEntity) {
        return WhatsAppMessageEntity.builder()
                .templateName("donation_alert")
                .phoneNumber(volunteerEntity.getPhoneNumber())
                .templateVariables(List.of(
                        request.getAlertLevel().name(),
                        ofNullable(request.getDoctorMessage()).orElse("")
                ))
                .build();
    }

    private static boolean receivesNotification(VolunteerEntity volunteerEntity, NotificationChannel notificationChannel) {
        return volunteerEntity.getNotificationChannels().contains(notificationChannel.name());
    }

    private static String doctorMessageOrEmpty(AlertEntity alert) {
        return ofNullable(alert.getDoctorMessage())
                .filter(StringUtils::isNotBlank)
                .map(" Doctor message: %s"::formatted)
                .orElse("");
    }

}
