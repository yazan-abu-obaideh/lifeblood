package org.otherband.lifeblood;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.invocation.Invocation;
import org.otherband.lifeblood.alert.AlertEntity;
import org.otherband.lifeblood.generated.model.*;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.otherband.lifeblood.jobs.AsyncNotificationService;
import org.otherband.lifeblood.notifications.push.PushNotification;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageEntity;
import org.otherband.lifeblood.volunteer.VolunteerEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.otherband.lifeblood.alert.AlertController.ALERT_API;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AlertTest extends BaseTest {

    @Test
    void invalidAlertRequest() throws Exception {
        AlertCreationRequest alertRequest = new AlertCreationRequest();
        String responseString = mockMvc.perform(
                        MockMvcRequestBuilders.post(ALERT_API)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(alertRequest))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse().getContentAsString();
        ErrorResponse errorResponse =
                objectMapper.readValue(responseString, ErrorResponse.class);
        assertThat(errorResponse.getErrorMessage())
                .contains("Alert level must be set")
                .contains("Hospital uuid must be provided");
    }

    @Test
    void getPaginatedAlerts() throws Exception {
        HospitalEntity[] hospitals = fetchAvailableHospitals();
        for (int i = 0; i < 10; i++) {
            AlertCreationRequest creationRequest = new AlertCreationRequest();
            creationRequest.setHospitalUuid(hospitals[0].getUuid());
            creationRequest.setAlertLevel(AlertLevel.ROUTINE);
            creationRequest.setDoctorMessage("");
            createAlert(creationRequest);
        }

        PageAlertResponse firstPage = getPageAlertResponse(Map.of());
        assertThat(firstPage.getNumber()).isEqualTo(0);
        assertThat(firstPage.getContent().size()).isEqualTo(10); // default page size
        PageAlertResponse secondPage = getPageAlertResponse(Map.of("pageNumber", "1", "pageSize", "1"));
        assertThat(secondPage.getNumber()).isEqualTo(1);
        assertThat(secondPage.getContent().size()).isEqualTo(1);
    }

    @Test
    void notifyAllWhenLifeOrDeath() throws Exception {
        List<VolunteerEntity> volunteers = createVolunteers();
        List<String> pushNotificationTokens = getPushNotificationTokens(volunteers);
        List<String> whatsAppPhoneNumbers = getWhatsAppPhoneNumbers(volunteers);
        assertThat(pushNotificationTokens.size()).isGreaterThan(0);
        assertThat(whatsAppPhoneNumbers.size()).isGreaterThan(0);

        HospitalEntity[] hospitals = fetchAvailableHospitals();
        AlertCreationRequest creationRequest = new AlertCreationRequest();
        creationRequest.setHospitalUuid(hospitals[0].getUuid());
        creationRequest.setAlertLevel(AlertLevel.LIFE_OR_DEATH);
        creationRequest.setDoctorMessage("Life or death alert");

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

        pushNotificationTokens.forEach(token ->
                verify(notificationSender, verificationData ->
                        findMatchingInvocation(verificationData, invocation -> {
                    if (invocation.getArgument(0) instanceof PushNotification pushNotification) {
                        return token.equals(pushNotification.getUserToken());
                    }
                    return false;
                })).sendPushNotification(any()));

        whatsAppPhoneNumbers.forEach(phoneNumber -> verify(notificationSender, verificationData ->
                findMatchingInvocation(verificationData, invocation -> {
            if (invocation.getArgument(0) instanceof WhatsAppMessageEntity whatsAppMessage) {
                return phoneNumber.equals(whatsAppMessage.getPhoneNumber());
            }
            return false;
        })).sendWhatsAppMessage(any()));

    }

    @ParameterizedTest
    @MethodSource("internationalDoctorMessages")
    void createAlert(String internationalDoctorMessage) throws Exception {
        HospitalEntity[] hospitals = fetchAvailableHospitals();
        AlertCreationRequest creationRequest = new AlertCreationRequest();
        creationRequest.setAlertLevel(AlertLevel.ROUTINE);
        creationRequest.setHospitalUuid(hospitals[0].getUuid());
        creationRequest.setDoctorMessage(internationalDoctorMessage);
        AlertEntity alert = createAlert(creationRequest);

        assertThat(alert.getAlertLevel()).isEqualTo(AlertLevel.ROUTINE);
        assertThat(alert.getHospital()).isNotNull();
        assertThat(alert.getHospital().getUuid()).isEqualTo(creationRequest.getHospitalUuid());
        assertThat(alert.getDoctorMessage()).isEqualTo(internationalDoctorMessage);

    }

    private PageAlertResponse getPageAlertResponse(Map<String, String> queryParams) throws Exception {
        String responseString = mockMvc.perform(
                        MockMvcRequestBuilders.get(ALERT_API)
                                .contentType("application/json")
                                .queryParams(MultiValueMap.fromSingleValue(queryParams))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(responseString, new TypeReference<>() {
        });
    }

    private AlertEntity createAlert(AlertCreationRequest creationRequest) throws Exception {
        String alertString = mockMvc.perform(
                        MockMvcRequestBuilders.post(ALERT_API)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(creationRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse().getContentAsString();

        return objectMapper.readValue(alertString, AlertEntity.class);
    }

    private List<VolunteerEntity> createVolunteers() throws Exception {
        ArrayList<VolunteerEntity> volunteers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            volunteers.add(createAnyVolunteer());
        }
        return volunteers;
    }

    private static void findMatchingInvocation(VerificationData verificationData, Predicate<Invocation> invocationFilter) {
        verificationData.getAllInvocations().stream().filter(invocationFilter)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Push notification not sent"));
    }

    private static List<String> getPushNotificationTokens(List<VolunteerEntity> startingVolunteers) {
        return startingVolunteers
                .stream().filter(volunteerEntity ->
                        volunteerEntity.getNotificationChannels().contains(NotificationChannel.PUSH_NOTIFICATIONS.name()))
                .map(VolunteerEntity::getPushNotificationToken)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    private static List<String> getWhatsAppPhoneNumbers(List<VolunteerEntity> startingVolunteers) {
        return startingVolunteers
                .stream().filter(volunteerEntity ->
                        volunteerEntity.getNotificationChannels().contains(NotificationChannel.WHATSAPP_MESSAGES.name()))
                .map(VolunteerEntity::getPhoneNumber)
                .toList();
    }

    public Stream<String> internationalDoctorMessages() {
        return Stream.of(
                "B- blood expected to run out within 2 weeks",
                "حالة خطيرة في المستشفى"
        );
    }
}
