package org.otherband.lifeblood.notifications;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.otherband.lifeblood.ProfileConstants;
import org.otherband.lifeblood.notifications.push.FirebaseNotificationSender;
import org.otherband.lifeblood.notifications.push.PushNotification;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageEntity;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageSender;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppSenderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Profile(ProfileConstants.DEVELOPMENT)
@Slf4j
public class DevNotificationConfig {

    @Bean
    public NotificationSender notificationSender(FirebaseNotificationSender firebaseNotificationSender,
                                                 WhatsAppMessageSender whatsAppMessageSender) {
        return new NotificationSender() {
            @Override
            public void sendPushNotification(PushNotification pushNotification) {
                log.info("Push notification [{}]", pushNotification);
            }

            @Override
            public void sendWhatsAppMessage(WhatsAppMessageEntity whatsAppMessage) {
                log.info("WhatsApp message [{}]", whatsAppMessage);
            }
        };
    }
}