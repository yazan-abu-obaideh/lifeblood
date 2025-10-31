package org.otherband.lifeblood.volunteer;

import jakarta.persistence.*;
import lombok.Data;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.otherband.lifeblood.notifications.push.PushNotificationType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity(name = "volunteer")
@EntityListeners(AuditingEntityListener.class)
public class VolunteerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uuid;

    private String phoneNumber;
    private boolean verifiedPhoneNumber;
    private boolean verifiedDonor;
    private LocalDateTime lastDonationDate;

    private String pushNotificationToken;
    @Enumerated(EnumType.STRING)
    private PushNotificationType pushNotificationType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "volunteer_hospital_mapping",
            joinColumns = @JoinColumn(name = "volunteer_id"),
            inverseJoinColumns = @JoinColumn(name = "hospital_id")
    )
    private List<HospitalEntity> alertableHospitals;
    private int minimumSeverity;

    @Column(columnDefinition = "varchar(50) array")
    private List<String> notificationChannels;

    @CreatedDate
    private LocalDateTime creationDate;
    @LastModifiedDate
    private LocalDateTime lastUpdatedDate;
}
