package org.otherband.notifications;

public interface GenericNotificationSender {

    void send(GenericNotification genericNotification);

    boolean canSend(RecipientDetails recipientDetails);
}
