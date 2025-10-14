package org.otherband.volunteer;

import jakarta.persistence.*;
import lombok.Data;
import org.otherband.entity.HospitalEntity;
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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "volunteer_hospital",
            joinColumns = @JoinColumn(name = "volunteer_uuid"),
            inverseJoinColumns = @JoinColumn(name = "hospital_uuid")
    )
    private List<HospitalEntity> alertableHospitals;

    @CreatedDate
    private LocalDateTime creationDate;
    @LastModifiedDate
    private LocalDateTime lastUpdatedDate;
}
