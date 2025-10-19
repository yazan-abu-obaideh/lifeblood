package org.otherband.lifeblood.volunteer;

public interface VerificationCodeSender {

    void send(PhoneNumberVerificationCodeEntity verificationCode);

}
