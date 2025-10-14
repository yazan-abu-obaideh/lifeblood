package org.otherband;

import org.junit.jupiter.api.Test;
import org.otherband.volunteer.VolunteerEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.otherband.VolunteerController.VOLUNTEER_API;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VolunteerRegistrationTest extends BaseTest {

    @Test
    public void createVolunteer() throws Exception {

        VolunteerEntity request = new VolunteerEntity();
        request.setUuid(UUID.randomUUID().toString());
        request.setPhoneNumber("+962-79-123-4567");

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
        assertThat(result.getUuid()).isNotEqualTo(request.getUuid());
    }

}
