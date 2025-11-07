package org.otherband.lifeblood.notifications.push;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.otherband.lifeblood.generated.model.PushNotificationType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity(name = "push_notification")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
