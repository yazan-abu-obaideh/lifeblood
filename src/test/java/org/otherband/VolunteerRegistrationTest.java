package org.otherband;

import org.junit.jupiter.api.Test;
import org.otherband.entity.HospitalEntity;
import org.otherband.volunteer.PhoneVerificationRequest;
import org.otherband.volunteer.VerificationCodeEntity;
import org.otherband.volunteer.VolunteerEntity;
import org.otherband.volunteer.VolunteerRegistrationRequest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.otherband.HospitalController.HOSPITAL_API;
import static org.otherband.volunteer.VolunteerController.VOLUNTEER_API;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        assertThat(errorResponse).contains("please enter your phone number");
        assertThat(errorResponse).contains("please choose at least one hospital of interest");
    }

    @Test
    void createVolunteer() throws Exception {
        String phoneNumber = "+962-79-123-4567";

        HospitalEntity[] availableHospitals = fetchAvailableHospitals();
        String hospitalUuid = availableHospitals[1].getUuid();

        VolunteerRegistrationRequest request = new VolunteerRegistrationRequest(
                phoneNumber,
                List.of(hospitalUuid)
        );

        VolunteerEntity result = createVolunteer(request);

        assertThat(result.getCreationDate()).isNotNull();
        assertThat(result.getLastUpdatedDate()).isNotNull();
        assertThat(result.getUuid()).isNotNull();
        assertThat(result.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(result.isVerifiedPhoneNumber()).isFalse();
        assertThat(result.getAlertableHospitals()).hasSize(1);
        HospitalEntity hospital = result.getAlertableHospitals().get(0);
        assertThat(hospital.getHospitalName()).isNotNull();
        assertThat(hospital.getUuid()).isEqualTo(hospitalUuid);
    }

    @Test
    void verifyPhoneNumber() throws Exception {
        String phoneNumber = "+962-77-123-4567";
        HospitalEntity[] availableHospitals = fetchAvailableHospitals();
        String hospitalUuid = availableHospitals[1].getUuid();

        VolunteerRegistrationRequest request = new VolunteerRegistrationRequest(
                phoneNumber,
                List.of(hospitalUuid)
        );

        VolunteerEntity volunteer = createVolunteer(request);

        assertThat(volunteer.isVerifiedPhoneNumber()).isFalse();

        VerificationCodeEntity verificationCode = verificationCodeJpaRepository.findAll()
                .stream()
                .filter(code -> phoneNumber.equals(code.getPhoneNumber()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Verification code not saved"));

        String code = verificationCode.getVerificationCode();

        mockMvc.perform(
                        MockMvcRequestBuilders.post(VOLUNTEER_API.concat("/verify-phone-number"))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(new PhoneVerificationRequest(
                                        code,
                                        phoneNumber
                                )))
                )
                .andExpect(status().isOk());

        VolunteerEntity updatedVolunteer = volunteerJpaRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AssertionError("Where did the volunteer disappear"));
        assertThat(updatedVolunteer.isVerifiedPhoneNumber()).isTrue();
    }

    private VolunteerEntity createVolunteer(VolunteerRegistrationRequest request) throws Exception {
        String responseString = mockMvc.perform(
                        MockMvcRequestBuilders.post(VOLUNTEER_API)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse().getContentAsString();

        return objectMapper.readValue(responseString, VolunteerEntity.class);
    }


}
