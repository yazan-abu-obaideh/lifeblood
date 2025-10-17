package org.otherband;

import org.mapstruct.Mapper;
import org.otherband.alert.AlertCreationRequest;
import org.otherband.alert.AlertEntity;
import org.otherband.volunteer.VolunteerEntity;
import org.otherband.volunteer.VolunteerRegistrationRequest;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    VolunteerEntity toEntity(VolunteerRegistrationRequest request);

    AlertEntity toEntity(AlertCreationRequest request);

}
