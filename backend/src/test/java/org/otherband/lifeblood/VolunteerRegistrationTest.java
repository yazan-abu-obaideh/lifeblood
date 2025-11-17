package org.otherband.lifeblood;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.otherband.lifeblood.generated.model.LoginRequest;
import org.otherband.lifeblood.generated.model.NotificationChannel;
import org.otherband.lifeblood.generated.model.PhoneVerificationRequest;
import org.otherband.lifeblood.generated.model.VolunteerRegistrationRequest;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.otherband.lifeblood.volunteer.PhoneNumberVerificationCodeEntity;
import org.otherband.lifeblood.volunteer.VolunteerEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.otherband.lifeblood.volunteer.VolunteerController.VOLUNTEER_API;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class VolunteerRegistrationTest extends BaseTest {


    @Test
    void requestValidations() throws Exception {
        String errorResponse = mockMvc.perform(
                        MockMvcRequestBuilders.post(VOLUNTEER_API)
                                .contentType("application/json")
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse().getContentAsString();
        assertThat(errorResponse)
                .contains("please enter your phone number")
                .contains("please choose at least one hospital of interest");
    }

    @Test
    void createVolunteer() throws Exception {
        String phoneNumber = randomPhoneNumber();

        HospitalEntity[] availableHospitals = fetchAvailableHospitals();
        String hospitalUuid = availableHospitals[1].getUuid();


        VolunteerRegistrationRequest request = new VolunteerRegistrationRequest();
        request.setPhoneNumber(phoneNumber);
        request.setSelectedHospitals(List.of(hospitalUuid));
        request.setPassword(randomPassword());

        VolunteerEntity result = createVolunteer(request);

        assertThat(result.getCreationDate()).isNotNull();
        assertThat(result.getLastUpdatedDate()).isNotNull();
        assertThat(result.getUuid()).isNotNull();
        assertThat(result.getMinimumSeverity()).isEqualTo(0);
        assertThat(result.getNotificationChannels().stream().map(NotificationChannel::valueOf))
                .containsAll(Arrays.asList(NotificationChannel.values()));
        assertThat(result.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(result.isVerifiedPhoneNumber()).isFalse();
        assertThat(result.getAlertableHospitals()).hasSize(1);
        HospitalEntity hospital = result.getAlertableHospitals().getFirst();
        assertThat(hospital.getHospitalName()).isNotNull();
        assertThat(hospital.getUuid()).isEqualTo(hospitalUuid);
    }

    @Test
    void verifyPhoneNumber() throws Exception {
        final String phoneNumber = randomPhoneNumber();
        final String password = randomPassword();

        HospitalEntity[] availableHospitals = fetchAvailableHospitals();
        String hospitalUuid = availableHospitals[1].getUuid();

        VolunteerRegistrationRequest request = new VolunteerRegistrationRequest();
        request.setSelectedHospitals(List.of(hospitalUuid));
        request.setPhoneNumber(phoneNumber);
        request.setPassword(password);

        VolunteerEntity volunteer = createVolunteer(request);

        assertThat(volunteer.isVerifiedPhoneNumber()).isFalse();

        PhoneNumberVerificationCodeEntity verificationCode = verificationCodeJpaRepository.findAll()
                .stream()
                .filter(code -> phoneNumber.equals(code.getPhoneNumber()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Verification code not saved"));

        String code = verificationCode.getVerificationCode();

        PhoneVerificationRequest phoneVerificationRequest = new PhoneVerificationRequest();
        phoneVerificationRequest.setPhoneNumber(phoneNumber);
        phoneVerificationRequest.setVerificationCode(code);
        mockMvc.perform(
                        MockMvcRequestBuilders.post(VOLUNTEER_API.concat("/verify-phone-number"))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(phoneVerificationRequest))
                )
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(phoneNumber);
        loginRequest.setPassword(password);

        var loginResponse = login(loginRequest);
        String authToken = getAuthToken(phoneNumber, loginResponse.getRefreshToken());


        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(authToken);
        String updatedResponseString = mockMvc.perform(
                        MockMvcRequestBuilders.get(VOLUNTEER_API.concat("/").concat(volunteer.getUuid()))
                                .headers(httpHeaders)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        VolunteerEntity updatedVolunteer = objectMapper.readValue(updatedResponseString,
                VolunteerEntity.class);

        assertThat(updatedVolunteer.isVerifiedPhoneNumber()).isTrue();
    }


}
