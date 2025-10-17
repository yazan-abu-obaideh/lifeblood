package org.otherband.lifeblood.alert;

import jakarta.persistence.*;
import lombok.Data;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Data
@Entity(name = "alert")
@EntityListeners(AuditingEntityListener.class)
public class AlertEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalDateTime fulfilmentDate;
    @Enumerated(EnumType.STRING)
    private AlertLevel alertLevel;
    private String doctorMessage;
    @CreatedDate
    private LocalDateTime creationDate;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_uuid", referencedColumnName = "uuid")
    private HospitalEntity hospital;
}
