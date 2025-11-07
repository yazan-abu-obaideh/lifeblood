package org.otherband.lifeblood;

import org.mapstruct.Mapper;
import org.otherband.lifeblood.alert.AlertCreationRequest;
import org.otherband.lifeblood.alert.AlertEntity;
import org.otherband.lifeblood.generated.model.*;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.otherband.lifeblood.volunteer.VolunteerEntity;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = {CustomDateTimeMapper.class})
public interface ApplicationMapper {

    VolunteerEntity toEntity(VolunteerRegistrationRequest request);

    VolunteerResponse toResponse(VolunteerEntity entity);

    HospitalResponse toResponse(HospitalEntity entity);

    AlertResponse toResponse(AlertEntity entity);

    PageAlertResponse toResponse(Page<AlertEntity> page);

    AlertEntity toEntity(AlertCreationRequest request);

}
