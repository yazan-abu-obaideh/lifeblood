package org.otherband.lifeblood;

import org.mapstruct.Mapper;
import org.otherband.lifeblood.alert.AlertCreationRequest;
import org.otherband.lifeblood.alert.AlertEntity;
import org.otherband.lifeblood.volunteer.VolunteerEntity;
import org.otherband.lifeblood.volunteer.VolunteerRegistrationRequest;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    VolunteerEntity toEntity(VolunteerRegistrationRequest request);

    AlertEntity toEntity(AlertCreationRequest request);

}
