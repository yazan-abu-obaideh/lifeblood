package org.otherband.lifeblood.jobs;

import lombok.extern.slf4j.Slf4j;
import org.otherband.lifeblood.notifications.NotificationSender;
import org.otherband.lifeblood.notifications.push.PushNotificationRepository;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class AsyncNotificationService {
    private final NotificationSender notificationSender;
    private final WhatsAppMessageRepository whatsAppMessageRepository;
    private final PushNotificationRepository pushNotificationRepository;
    private final AtomicLong notificationsSent = new AtomicLong();


    public AsyncNotificationService(NotificationSender notificationSender,
                                    WhatsAppMessageRepository whatsAppMessageRepository,
                                    PushNotificationRepository pushNotificationRepository) {
        this.notificationSender = notificationSender;
        this.whatsAppMessageRepository = whatsAppMessageRepository;
        this.pushNotificationRepository = pushNotificationRepository;
    }

    @Scheduled(fixedDelayString = "${notifications.fixed.delay.milli.seconds}")
    public void sendNotifications() {
        notificationsSent.set(0);
        whatsAppMessageRepository.findTop100BySentIsFalseOrderByCreationDateAsc()
                .forEach(whatsAppMessage -> {
                    notificationSender.sendWhatsAppMessage(whatsAppMessage);
                    notificationsSent.getAndIncrement();
                });
        pushNotificationRepository.findTop100BySentIsFalseOrderByCreationDateAsc()
                .forEach(pushNotification -> {
                    notificationSender.sendPushNotification(pushNotification);
                    notificationsSent.getAndIncrement();
                });
        if (notificationsSent.get() > 0) {
            log.info("[{}] sent [{}] notifications successfully.", this.getClass(), notificationsSent.get());
        }
    }

}
