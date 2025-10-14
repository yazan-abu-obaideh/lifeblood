package org.otherband;

import org.junit.jupiter.api.Test;
import org.otherband.entity.HospitalEntity;
import org.otherband.volunteer.VolunteerEntity;
import org.otherband.volunteer.VolunteerRegistrationRequest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.otherband.HospitalController.HOSPITAL_API;
import static org.otherband.VolunteerController.VOLUNTEER_API;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VolunteerRegistrationTest extends BaseTest {

    @Test
    public void createVolunteer() throws Exception {
        String phoneNumber = "+962-79-123-4567";

        HospitalEntity[] availableHospitals = objectMapper.readValue(mockMvc.perform(
                                MockMvcRequestBuilders.get(HOSPITAL_API)
                                        .contentType("application/json")
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse().getContentAsString(),
                HospitalEntity[].class);
        String hospitalUuid = availableHospitals[1].getUuid();

        VolunteerRegistrationRequest request = new VolunteerRegistrationRequest();
        request.setPhoneNumber(phoneNumber);
        request.setSelectedHospitals(
                List.of(hospitalUuid)
        );

        String responseString = mockMvc.perform(
                        MockMvcRequestBuilders.post(VOLUNTEER_API)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse().getContentAsString();

        VolunteerEntity result = objectMapper.readValue(responseString, VolunteerEntity.class);
        assertThat(result.getCreationDate()).isNotNull();
        assertThat(result.getLastUpdatedDate()).isNotNull();
        assertThat(result.getUuid()).isNotNull();
        assertThat(result.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(result.getAlertableHospitals()).hasSize(1);
        HospitalEntity hospital = result.getAlertableHospitals().get(0);
        assertThat(hospital.getHospitalName()).isNotNull();
        assertThat(hospital.getUuid()).isEqualTo(hospitalUuid);
    }

}
