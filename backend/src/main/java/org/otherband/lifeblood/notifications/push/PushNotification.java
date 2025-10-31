package org.otherband.lifeblood.notifications.push;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity(name = "push_notification")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
public class PushNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String body;
    private String userToken;
    private boolean sent;

    @Enumerated(EnumType.STRING)
    private PushNotificationType pushNotificationType;

    @CreatedDate
    private LocalDateTime creationDate;
}
