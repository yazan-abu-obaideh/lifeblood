package org.otherband.notifications;

public record GenericNotification(
        String title,
        String body,
        RecipientDetails recipientDetails
) {
}
