package org.otherband.lifeblood.notifications.whatsapp;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class WhatsAppMessageSender {

    private final RestTemplate restTemplate;
    private final WhatsAppSenderConfig config;

    public WhatsAppMessageSender(RestTemplate restTemplate, WhatsAppSenderConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public void send(WhatsAppMessageEntity genericNotification) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.bearerToken());
            restTemplate.postForEntity(config.whatsappApiUrl(),
                    new HttpEntity<>(buildMessage(genericNotification.getPhoneNumber(),
                            genericNotification.getTemplateName()), headers),
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

    private record WhatsAppMessage(
            String messaging_product,
            String to,
            String type,
            Template template
    ) {
    }

    private record Template(
            String name,
            Language language) {
    }

    private record Language(String code) {
    }

}
