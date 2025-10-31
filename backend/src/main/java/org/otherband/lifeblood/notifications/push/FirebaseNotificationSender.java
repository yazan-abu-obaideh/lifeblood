package org.otherband.lifeblood.notifications.push;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

public class FirebaseNotificationSender {

    public void send(PushNotification notification) {
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
