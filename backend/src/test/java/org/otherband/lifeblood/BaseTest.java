package org.otherband.lifeblood;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.otherband.lifeblood.hospital.HospitalJpaRepository;
import org.otherband.lifeblood.notifications.NotificationSender;
import org.otherband.lifeblood.notifications.push.PushNotificationRepository;
import org.otherband.lifeblood.notifications.push.PushNotificationType;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.otherband.lifeblood.volunteer.VerificationCodeJpaRepository;
import org.otherband.lifeblood.volunteer.VolunteerEntity;
import org.otherband.lifeblood.volunteer.VolunteerJpaRepository;
import org.otherband.lifeblood.volunteer.VolunteerRegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.otherband.lifeblood.hospital.HospitalController.HOSPITAL_API;
import static org.otherband.lifeblood.volunteer.VolunteerController.VOLUNTEER_API;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.profiles.active=test", "app.config.jobs.enabled=false"})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    public static final Faker FAKER = new Faker();

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    public HospitalJpaRepository hospitalJpaRepository;

    @Autowired
    public VolunteerJpaRepository volunteerJpaRepository;

    @MockitoBean
    public NotificationSender notificationSender;

    @Autowired
    public VerificationCodeJpaRepository verificationCodeJpaRepository;

    @Autowired
    public WhatsAppMessageRepository whatsAppMessageRepository;

    @Autowired
    public PushNotificationRepository pushNotificationRepository;

    @Value("classpath:hospitals.json")
    private Resource hospitals;

    @BeforeAll
    public void initialData() throws IOException {
        String hospitals = this.hospitals.getContentAsString(StandardCharsets.UTF_8);
        HospitalEntity[] hospitalEntities = objectMapper.readValue(hospitals, HospitalEntity[].class);
        hospitalJpaRepository.saveAll(Arrays.asList(hospitalEntities));
    }

    public VolunteerEntity createAnyVolunteer() throws Exception {
        String chosenHospitalUuid = Arrays.stream(fetchAvailableHospitals()).findAny().map(HospitalEntity::getUuid)
                .orElseThrow(() -> new AssertionFailure("Expected to find at least one hospital"));
        return createVolunteer(new VolunteerRegistrationRequest(
                FAKER.phoneNumber().phoneNumber(),
                List.of(chosenHospitalUuid),
                UUID.randomUUID().toString(),
                PushNotificationType.FIREBASE
        ));
    }

    protected HospitalEntity[] fetchAvailableHospitals() throws Exception {
        String contentAsString = mockMvc.perform(MockMvcRequestBuilders.get(HOSPITAL_API).contentType("application/json")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(contentAsString, HospitalEntity[].class);
    }


    protected VolunteerEntity createVolunteer(VolunteerRegistrationRequest request) throws Exception {
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