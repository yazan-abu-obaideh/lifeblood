package org.otherband.lifeblood;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;
import org.otherband.lifeblood.alert.AlertCreationRequest;
import org.otherband.lifeblood.alert.AlertEntity;
import org.otherband.lifeblood.alert.AlertLevel;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.otherband.lifeblood.jobs.AsyncNotificationService;
import org.otherband.lifeblood.notifications.NotificationChannel;
import org.otherband.lifeblood.volunteer.VolunteerEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.otherband.lifeblood.alert.AlertController.ALERT_API;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AlertTest extends BaseTest {

    @Test
    void notifyAllWhenLifeOrDeath() throws Exception {
        List<VolunteerEntity> startingVolunteers = volunteerJpaRepository.findAll();
        int countWhatsApp = (int) countWhatsApp(startingVolunteers);
        int countPush = (int) countPush(startingVolunteers);

        if ((countPush + countWhatsApp) == 0) {
            List<VolunteerEntity> volunteers = createVolunteers();
            countPush = (int) countPush(volunteers);
            countWhatsApp = (int) countWhatsApp(volunteers);
            assertThat(countPush + countWhatsApp).isGreaterThan(0);
        }

        HospitalEntity[] hospitals = fetchAvailableHospitals();
        AlertCreationRequest creationRequest = new AlertCreationRequest(
                hospitals[0].getUuid(),
                AlertLevel.LIFE_OR_DEATH,
                "Life or death alert"
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(ALERT_API)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(creationRequest))
                )
                .andExpect(status().isCreated());

        new AsyncNotificationService(
                notificationSender,
                whatsAppMessageRepository,
                pushNotificationRepository
        ).sendNotifications();

        /* our tests run in parallel, extra users may be created */
        verify(notificationSender, atLeast(countWhatsApp)).sendWhatsAppMessage(any());
        verify(notificationSender, atLeast(countPush)).sendPushNotification(any());
    }

    private static long countPush(List<VolunteerEntity> startingVolunteers) {
        return startingVolunteers
                .stream().filter(volunteerEntity2 ->
                        volunteerEntity2.getNotificationChannels().contains(NotificationChannel.PUSH_NOTIFICATIONS.name()))
                .filter(volunteerEntity -> StringUtils.isNotBlank(volunteerEntity.getPushNotificationToken()))
                .count();
    }

    private static long countWhatsApp(List<VolunteerEntity> startingVolunteers) {
        return startingVolunteers
                .stream().filter(volunteerEntity3 ->
                        volunteerEntity3.getNotificationChannels().contains(NotificationChannel.WHATSAPP_MESSAGES.name()))
                .count();
    }

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

    private List<VolunteerEntity> createVolunteers() throws Exception {
        ArrayList<VolunteerEntity> volunteers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            volunteers.add(createAnyVolunteer());
        }
        return volunteers;
    }

    public Stream<String> doctorMessage() {
        return Stream.of(
                "B- blood expected to run out within 2 weeks",
                "حالة خطيرة في المستشفى"
        );
    }
}
