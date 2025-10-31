package org.otherband.lifeblood.notifications;

import org.otherband.lifeblood.notifications.push.FirebaseNotificationSender;
import org.otherband.lifeblood.notifications.push.PushNotification;
import org.otherband.lifeblood.notifications.push.PushNotificationRepository;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageSender;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageEntity;

public class DelegatingNotificationSender implements NotificationSender {

    private final WhatsAppMessageSender whatsAppMessageSender;
    private final FirebaseNotificationSender firebaseNotificationSender;
    private final PushNotificationRepository pushNotificationRepository;
    private final WhatsAppMessageRepository whatsAppMessageRepository;

    public DelegatingNotificationSender(WhatsAppMessageSender whatsAppMessageSender,
                                        FirebaseNotificationSender firebaseNotificationSender,
                                        PushNotificationRepository pushNotificationRepository,
                                        WhatsAppMessageRepository whatsAppMessageRepository) {
        this.whatsAppMessageSender = whatsAppMessageSender;
        this.firebaseNotificationSender = firebaseNotificationSender;
        this.pushNotificationRepository = pushNotificationRepository;
        this.whatsAppMessageRepository = whatsAppMessageRepository;
    }

    @Override
    public void sendPushNotification(PushNotification pushNotification) {
        switch (pushNotification.getPushNotificationType()) {
            case FIREBASE -> {
                firebaseNotificationSender.send(pushNotification);
                pushNotification.setSent(true);
                pushNotificationRepository.save(pushNotification);
            }
            case APPLE_PUSH_NOTIFICATION -> throw new UnsupportedOperationException("APN not yet implemented");
            case null -> throw new IllegalArgumentException("Push notification must have a type");
        }
    }

    @Override
    public void sendWhatsAppMessage(WhatsAppMessageEntity whatsAppMessage) {
        whatsAppMessageSender.send(whatsAppMessage);
        whatsAppMessage.setSent(true);
        whatsAppMessageRepository.save(whatsAppMessage);
    }

}
