package org.otherband.lifeblood.jobs;

import org.otherband.lifeblood.notifications.NotificationSender;
import org.otherband.lifeblood.notifications.push.PushNotificationRepository;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.config.jobs.enabled", havingValue = "true")
public class JobsConfig {

    @Bean
    public AsyncNotificationService asyncNotificationService(NotificationSender delegatingNotificationSender,
                                                             WhatsAppMessageRepository whatsAppMessageRepository,
                                                             PushNotificationRepository pushNotificationRepository) {
        return new AsyncNotificationService(delegatingNotificationSender,
                whatsAppMessageRepository,
                pushNotificationRepository);
    }

}
