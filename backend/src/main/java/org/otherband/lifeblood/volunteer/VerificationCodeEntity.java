package org.otherband.lifeblood.volunteer;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Data
@Entity(name = "phone_number_verification_code")
@EntityListeners(AuditingEntityListener.class)
public class VerificationCodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String phoneNumber;
    private String verificationCode;
    @CreatedDate
    private LocalDateTime creationDate;

}
