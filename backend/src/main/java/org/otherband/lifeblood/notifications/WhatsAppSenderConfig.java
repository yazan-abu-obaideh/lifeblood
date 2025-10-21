package org.otherband.lifeblood.notifications;

public record WhatsAppSenderConfig(
        String whatsappApiUrl,
        String senderPhoneId,
        String bearerToken,
        String templateName
) {
}
