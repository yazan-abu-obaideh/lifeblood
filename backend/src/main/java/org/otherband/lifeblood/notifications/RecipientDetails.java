package org.otherband.lifeblood.notifications;

import lombok.Builder;

@Builder
public record RecipientDetails(
        String phoneNumber,
        String firebaseToken,
        String iosToken,
        String emailAddress
) {
}
