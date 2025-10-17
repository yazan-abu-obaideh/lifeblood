package org.otherband.lifeblood;

import org.junit.jupiter.api.Test;
import org.otherband.lifeblood.alert.AlertCreationRequest;
import org.otherband.lifeblood.alert.AlertEntity;
import org.otherband.lifeblood.alert.AlertLevel;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.otherband.lifeblood.alert.AlertController.ALERT_API;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AlertTest extends BaseTest {

    @Test
    void createAlert() throws Exception {
        HospitalEntity[] hospitals = fetchAvailableHospitals();
        AlertCreationRequest creationRequest = new AlertCreationRequest(
                hospitals[0].getUuid(),
                AlertLevel.ROUTINE,
                "B- blood expected to run out within 2 weeks"
        );
        String alertString = mockMvc.perform(
                        MockMvcRequestBuilders.post(ALERT_API)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(creationRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse().getContentAsString();

        AlertEntity alert = objectMapper.readValue(alertString, AlertEntity.class);

        assertThat(alert.getAlertLevel()).isEqualTo(AlertLevel.ROUTINE);
        assertThat(alert.getHospital()).isNotNull();
        assertThat(alert.getHospital().getUuid()).isEqualTo(creationRequest.hospitalUuid());
        assertThat(alert.getDoctorMessage()).isEqualTo("B- blood expected to run out within 2 weeks");

    }

}
