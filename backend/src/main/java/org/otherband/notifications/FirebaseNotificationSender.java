package org.otherband.notifications;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.apache.commons.lang3.StringUtils;

public class FirebaseNotificationSender implements GenericNotificationSender {

    @Override
    public boolean canSend(RecipientDetails recipientDetails) {
        return !StringUtils.isBlank(recipientDetails.firebaseToken());
    }

    @Override
    public void send(GenericNotification genericNotification) {
        Message message = Message.builder()
                .setToken(genericNotification.recipientDetails().firebaseToken())
                .setNotification(Notification.builder()
                        .setTitle(genericNotification.title())
                        .setBody(genericNotification.body())
                        .build())
                .build();
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
