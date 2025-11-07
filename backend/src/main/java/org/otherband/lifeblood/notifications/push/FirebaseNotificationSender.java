package org.otherband.lifeblood.notifications.push;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.hibernate.AssertionFailure;

public class FirebaseNotificationSender {

    public void send(PushNotification notification) {
        PushNotificationType notificationType = notification.getPushNotificationType();
        switch (notificationType) {
            case FIREBASE -> sendFireBase(notification);
            case APPLE_PUSH_NOTIFICATION ->
                    throw new UnsupportedOperationException("Apple push notifications not yet supported");
            default ->
                    throw new AssertionFailure("Expected push notification type, got [%s] instead".formatted(notificationType));
        }
    }

    private void sendFireBase(PushNotification notification) {
        Message message = Message.builder()
                .setToken(notification.getUserToken())
                .setNotification(Notification.builder()
                        .setTitle(notification.getTitle())
                        .setBody(notification.getBody())
                        .build())
                .build();
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
