package org.otherband.lifeblood.notifications;

import org.otherband.lifeblood.notifications.push.FirebaseNotificationSender;
import org.otherband.lifeblood.notifications.push.PushNotification;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageSender;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageEntity;

public class DelegatingNotificationSender implements NotificationSender {

    private final WhatsAppMessageSender whatsAppMessageSender;

    private final FirebaseNotificationSender firebaseNotificationSender;

    public DelegatingNotificationSender(WhatsAppMessageSender whatsAppMessageSender, FirebaseNotificationSender firebaseNotificationSender) {
        this.whatsAppMessageSender = whatsAppMessageSender;
        this.firebaseNotificationSender = firebaseNotificationSender;
    }

    @Override
    public void sendPushNotification(PushNotification pushNotification) {
        switch (pushNotification.getPushNotificationType()) {
            case FIREBASE -> firebaseNotificationSender.send(pushNotification);
            case APPLE_PUSH_NOTIFICATION -> throw new UnsupportedOperationException("APN not yet implemented");
            case null -> throw new IllegalArgumentException("Push notification must have a type");
        }
    }

    @Override
    public void sendWhatsAppMessage(WhatsAppMessageEntity whatsAppMessage) {
        whatsAppMessageSender.send(whatsAppMessage);
    }

}
