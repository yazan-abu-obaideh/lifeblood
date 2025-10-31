package org.otherband.lifeblood.notifications.whatsapp;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "whatsapp_message")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
public class WhatsAppMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String templateName;
    @Column(columnDefinition = "varchar(50) array")
    private List<String> templateVariables;

    private String phoneNumber;
    private boolean sent;

    @CreatedDate
    private LocalDateTime creationDate;
}
