package org.otherband.lifeblood.jobs;

import org.otherband.lifeblood.notifications.NotificationSender;
import org.otherband.lifeblood.notifications.push.PushNotificationRepository;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.springframework.scheduling.annotation.Scheduled;

public class AsyncNotificationService {
    private final NotificationSender delegatingNotificationSender;
    private final WhatsAppMessageRepository whatsAppMessageRepository;
    private final PushNotificationRepository pushNotificationRepository;


    public AsyncNotificationService(NotificationSender delegatingNotificationSender,
                                    WhatsAppMessageRepository whatsAppMessageRepository,
                                    PushNotificationRepository pushNotificationRepository) {
        this.delegatingNotificationSender = delegatingNotificationSender;
        this.whatsAppMessageRepository = whatsAppMessageRepository;
        this.pushNotificationRepository = pushNotificationRepository;
    }

    @Scheduled(fixedDelayString = "${notifications.fixed.delay.nano.seconds}")
    public void sendNotifications() {
        whatsAppMessageRepository.findTop100BySentIsFalseOrderByCreationDateAsc()
                .forEach(delegatingNotificationSender::sendWhatsAppMessage);
        pushNotificationRepository.findTop100BySentIsFalseOrderByCreationDateAsc()
                .forEach(delegatingNotificationSender::sendPushNotification);
    }

}
