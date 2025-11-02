package org.otherband.lifeblood.notifications;

import lombok.extern.slf4j.Slf4j;
import org.otherband.lifeblood.ProfileConstants;
import org.otherband.lifeblood.notifications.push.PushNotification;
import org.otherband.lifeblood.notifications.push.PushNotificationRepository;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageEntity;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(ProfileConstants.DEVELOPMENT)
@Slf4j
public class DevNotificationConfig {

    @Bean
    public NotificationSender notificationSender(WhatsAppMessageRepository whatsAppMessageRepository, PushNotificationRepository pushNotificationRepository) {
        return new NotificationSender() {
            @Override
            public void sendPushNotification(PushNotification pushNotification) {
                log.info("Push notification [{}]", pushNotification);
                pushNotification.setSent(true);
                pushNotificationRepository.save(pushNotification);
            }

            @Override
            public void sendWhatsAppMessage(WhatsAppMessageEntity whatsAppMessage) {
                log.info("WhatsApp message [{}]", whatsAppMessage);
                whatsAppMessage.setSent(true);
                whatsAppMessageRepository.save(whatsAppMessage);
            }
        };
    }
}