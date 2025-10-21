package org.otherband.lifeblood.notifications;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class WhatsAppMessageSender implements GenericNotificationSender {

    private final RestTemplate restTemplate;
    private final WhatsAppSenderConfig config;

    public WhatsAppMessageSender(RestTemplate restTemplate, WhatsAppSenderConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    @Override
    public boolean canSend(RecipientDetails recipientDetails) {
        return !StringUtils.isBlank(recipientDetails.phoneNumber());
    }

    @Override
    public void send(GenericNotification genericNotification) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.bearerToken());
            restTemplate.postForEntity(config.whatsappApiUrl(),
                    new HttpEntity<>(
                            buildMessage(genericNotification.recipientDetails().phoneNumber(),
                                    config.templateName()), headers),
                    Void.class,
                    config.senderPhoneId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send WhatsApp message", e);
        }
    }

    private static WhatsAppMessage buildMessage(String receiverNumber, String templateName) {
        return new WhatsAppMessage(
                "whatsapp",
                receiverNumber,
                "template",
                new Template(
                        templateName,
                        new Language("en_US")
                )
        );
    }

    record WhatsAppMessage(
            String messaging_product,
            String to,
            String type,
            Template template
    ) {}

    record Template(
            String name,
            Language language) {}

    record Language(String code) {}

}
