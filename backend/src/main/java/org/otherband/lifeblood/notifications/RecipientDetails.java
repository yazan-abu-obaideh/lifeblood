package org.otherband.lifeblood.notifications;

public record RecipientDetails(
        String phoneNumber,
        String firebaseToken,
        String iosToken,
        String emailAddress
) {
}
