package org.otherband.lifeblood.notifications;

public record GenericNotification(
        String title,
        String body,
        RecipientDetails recipientDetails
) {
}
