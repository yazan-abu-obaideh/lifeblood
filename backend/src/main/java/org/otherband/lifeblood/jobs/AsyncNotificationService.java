package org.otherband.lifeblood.jobs;

import lombok.extern.slf4j.Slf4j;
import org.otherband.lifeblood.notifications.NotificationSender;
import org.otherband.lifeblood.notifications.push.PushNotificationRepository;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class AsyncNotificationService {
    private final NotificationSender notificationSender;
    private final WhatsAppMessageRepository whatsAppMessageRepository;
    private final PushNotificationRepository pushNotificationRepository;


    public AsyncNotificationService(NotificationSender notificationSender,
                                    WhatsAppMessageRepository whatsAppMessageRepository,
                                    PushNotificationRepository pushNotificationRepository) {
        this.notificationSender = notificationSender;
        this.whatsAppMessageRepository = whatsAppMessageRepository;
        this.pushNotificationRepository = pushNotificationRepository;
    }

    @Scheduled(fixedDelayString = "${notifications.fixed.delay.milli.seconds}")
    public void sendNotifications() {
        log.info("[{}] is sending notifications...", this.getClass());
        whatsAppMessageRepository.findTop100BySentIsFalseOrderByCreationDateAsc()
                .parallelStream()
                .forEach(notificationSender::sendWhatsAppMessage);
        pushNotificationRepository.findTop100BySentIsFalseOrderByCreationDateAsc()
                .parallelStream()
                .forEach(notificationSender::sendPushNotification);
        log.info("[{}] finished sending notifications.", this.getClass());
    }

}
