package org.otherband.lifeblood.notifications.whatsapp;

public record WhatsAppSenderConfig(
        String whatsappApiUrl,
        String senderPhoneId,
        String bearerToken,
        String templateName
) {
}
