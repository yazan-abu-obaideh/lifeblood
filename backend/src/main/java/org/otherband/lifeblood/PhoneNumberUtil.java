package org.otherband.lifeblood;

import com.google.i18n.phonenumbers.NumberParseException;
import lombok.extern.slf4j.Slf4j;

import static com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import static com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance;

@Slf4j
public enum PhoneNumberUtil {
    INSTANCE;

    private static final PhoneNumberFormat FORMAT = PhoneNumberFormat.E164;
    private static final String CURR_REGION = "JO";

    public String formatPhoneNumber(String phoneNumber) {
        try {
            return getInstance().format(getInstance().parse(phoneNumber, CURR_REGION), FORMAT);
        } catch (NumberParseException e) {
            log.error("Failed to format phone number", e);
            throw new UserException("Failed to format phone number '%s'".formatted(phoneNumber));
        }
    }
}
