package org.otherband.lifeblood.notifications;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.otherband.lifeblood.ProfileConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Configuration
@Profile(ProfileConstants.PRODUCTION)
public class NotificationConfig {

    private final Resource firebaseAdminJson;

    public NotificationConfig(@Value("classpath:blood-banker-firebase-admin.json") Resource firebaseAdminJson) {
        this.firebaseAdminJson = firebaseAdminJson;
    }

    /**
     * Received every implementation of GenericNotificationSender (e.g. WhatsApp, Firebase, iOS, etc)
     */
    @Bean
    public NotificationSender notificationSender(List<GenericNotificationSender> genericSenders) {
        return new NotificationSender(genericSenders);
    }

    @Bean
    public FirebaseNotificationSender fireBaseNotificationSender() {
        return new FirebaseNotificationSender();
    }

    @Bean
    public WhatsAppMessageSender whatsAppMessageSender(
            @Value("${whatsapp.api.url}") String whatsappApiUrl,
            @Value("${whatsapp.sender.phone.id}") String senderPhoneId,
            @Value("${whatsapp.api.bearer.token}") String bearerToken,
            @Value("${whatsapp.template.name}") String templateName
    ) {
        return new WhatsAppMessageSender(new RestTemplate(), new WhatsAppSenderConfig(
                whatsappApiUrl,
                senderPhoneId,
                bearerToken,
                templateName
        ));
    }

    @PostConstruct
    public void initializeFirebase() throws IOException {
        FileInputStream serviceAccount = new FileInputStream(firebaseAdminJson.getFile());
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        FirebaseApp.initializeApp(options);
    }
}