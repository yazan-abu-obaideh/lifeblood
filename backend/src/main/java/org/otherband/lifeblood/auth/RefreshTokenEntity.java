package org.otherband.lifeblood.auth;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity(name = "refresh_token")
@Data
@EntityListeners(AuditingEntityListener.class)
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tokenHash;
    private String username;
    @CreatedDate
    private LocalDateTime creationDate;
}
