package org.otherband.volunteer;

public record PhoneVerificationRequest(String verificationCode, String phoneNumber) {
}
