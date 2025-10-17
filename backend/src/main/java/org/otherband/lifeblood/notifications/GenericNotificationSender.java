package org.otherband.lifeblood.notifications;

public interface GenericNotificationSender {

    void send(GenericNotification genericNotification);

    boolean canSend(RecipientDetails recipientDetails);
}
