package org.otherband.lifeblood.notifications;

import org.otherband.lifeblood.notifications.push.PushNotificationRepository;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
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
                .parallelStream()
                .forEach(delegatingNotificationSender::sendWhatsAppMessage);
        pushNotificationRepository.findTop100BySentIsFalseOrderByCreationDateAsc()
                .parallelStream()
                .forEach(delegatingNotificationSender::sendPushNotification);
    }

}
