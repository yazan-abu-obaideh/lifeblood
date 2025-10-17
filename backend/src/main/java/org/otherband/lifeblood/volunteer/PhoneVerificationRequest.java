package org.otherband.lifeblood.volunteer;

public record PhoneVerificationRequest(String verificationCode, String phoneNumber) {
}
