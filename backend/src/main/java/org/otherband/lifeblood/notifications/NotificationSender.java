package org.otherband.lifeblood.notifications;

import org.otherband.lifeblood.notifications.push.PushNotification;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageEntity;

public interface NotificationSender {
    void sendPushNotification(PushNotification pushNotification);
    void sendWhatsAppMessage(WhatsAppMessageEntity whatsAppMessage);
}
