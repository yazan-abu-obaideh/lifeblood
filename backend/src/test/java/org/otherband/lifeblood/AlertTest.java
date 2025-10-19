package org.otherband.lifeblood;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.otherband.lifeblood.alert.AlertCreationRequest;
import org.otherband.lifeblood.alert.AlertEntity;
import org.otherband.lifeblood.alert.AlertLevel;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.otherband.lifeblood.alert.AlertController.ALERT_API;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AlertTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("doctorMessage")
    void createAlert(String doctorMessage) throws Exception {
        HospitalEntity[] hospitals = fetchAvailableHospitals();
        AlertCreationRequest creationRequest = new AlertCreationRequest(
                hospitals[0].getUuid(),
                AlertLevel.ROUTINE,
                doctorMessage
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
        assertThat(alert.getDoctorMessage()).isEqualTo(doctorMessage);

    }

    public Stream<String> doctorMessage() {
        return Stream.of(
                "B- blood expected to run out within 2 weeks",
                "حالة خطيرة في المستشفى"
        );
    }
}
