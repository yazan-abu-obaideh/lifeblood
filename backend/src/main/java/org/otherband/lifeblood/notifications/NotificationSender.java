package org.otherband.lifeblood.notifications;

import org.otherband.lifeblood.volunteer.PhoneNumberVerificationCodeEntity;
import org.otherband.lifeblood.volunteer.VerificationCodeSender;

import java.util.List;

public class NotificationSender implements VerificationCodeSender {

    private final List<GenericNotificationSender> genericSenders;

    public NotificationSender(List<GenericNotificationSender> genericSenders) {
        this.genericSenders = genericSenders;
    }

    @Override
    public void send(PhoneNumberVerificationCodeEntity verificationCode) {
        RecipientDetails details = RecipientDetails.builder().phoneNumber(verificationCode.getPhoneNumber()).build();
        genericSenders.forEach(sender -> {
            if (sender.canSend(details)) {
                GenericNotification notification = new GenericNotification(
                        "Verification Code Received",
                        "Your verification code is '%s'".formatted(verificationCode.getVerificationCode()),
                        details);
                sender.send(notification);
            }
        });
    }
}
