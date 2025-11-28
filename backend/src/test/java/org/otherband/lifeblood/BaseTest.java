package org.otherband.lifeblood;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.otherband.lifeblood.auth.AuthEntity;
import org.otherband.lifeblood.auth.AuthenticationJpaRepository;
import org.otherband.lifeblood.auth.RoleConstants;
import org.otherband.lifeblood.generated.model.*;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.otherband.lifeblood.hospital.HospitalJpaRepository;
import org.otherband.lifeblood.notifications.NotificationSender;
import org.otherband.lifeblood.notifications.push.PushNotificationRepository;
import org.otherband.lifeblood.notifications.whatsapp.WhatsAppMessageRepository;
import org.otherband.lifeblood.volunteer.VerificationCodeJpaRepository;
import org.otherband.lifeblood.volunteer.VolunteerEntity;
import org.otherband.lifeblood.volunteer.VolunteerJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.otherband.lifeblood.auth.AuthController.AUTH_API;
import static org.otherband.lifeblood.hospital.HospitalController.HOSPITAL_API;
import static org.otherband.lifeblood.volunteer.VolunteerController.VOLUNTEER_API;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "app.config.jobs.enabled=false",
        "jwt.secret.key=SOME_TOP_SECRET_JWT_KEY_THAT_MUST_NOT_BE_LEAKED",
        "jwt.token.expiration.minutes=15"
})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public abstract class BaseTest {

    public static final Faker FAKER = new Faker();
    public static final String DOCTOR_PHONE_NUMBER = "+962797777777";
    public static final String DOCTOR_PASSWORD = randomPassword();

    public static final AtomicBoolean SETUP_DONE = new AtomicBoolean();

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

    @Autowired
    private AuthenticationJpaRepository authenticationJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeAll
    public void initialData() {
        doSetup(authenticationJpaRepository, passwordEncoder);
    }

    private static synchronized void doSetup(AuthenticationJpaRepository authenticationJpaRepository,
                                             PasswordEncoder passwordEncoder) {
          /* because the test instance life cycle is per class,
           the setup method runs multiple times.
           This synchronization and boolean check prevent that.
         */
        if (!SETUP_DONE.get()) {
            authenticationJpaRepository.save(
                    AuthEntity.builder()
                            .phoneNumber(DOCTOR_PHONE_NUMBER)
                            .userUuid(UUID.randomUUID().toString())
                            .hashedPassword(passwordEncoder.encode(DOCTOR_PASSWORD))
                            .roles(Set.of(RoleConstants.DOCTOR_ROLE))
                            .build()
            );
            SETUP_DONE.set(true);
        }
    }

    @SneakyThrows
    public LoginResponse login(LoginRequest loginRequest) {
        String responseString = mockMvc.perform(
                        MockMvcRequestBuilders.post(AUTH_API.concat("/login"))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        return objectMapper.readValue(responseString, LoginResponse.class);
    }

    @SneakyThrows
    public String getAuthToken(String phoneNumber, String refreshToken) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setPhoneNumber(phoneNumber);
        request.setRefreshToken(refreshToken);
        return mockMvc.perform(
                        MockMvcRequestBuilders.post(AUTH_API.concat("/refresh"))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    public static String randomPassword() {
        return RandomStringUtils.secure().next(16);
    }

    public VolunteerEntity createAnyVolunteer() throws Exception {
        String chosenHospitalUuid = Arrays.stream(fetchAvailableHospitals()).findAny().map(HospitalEntity::getUuid)
                .orElseThrow(() -> new AssertionFailure("Expected to find at least one hospital"));
        VolunteerRegistrationRequest registrationRequest = new VolunteerRegistrationRequest();
        registrationRequest.setPhoneNumber(randomPhoneNumber());
        registrationRequest.setSelectedHospitals(List.of(chosenHospitalUuid));
        registrationRequest.setPushNotificationToken(UUID.randomUUID().toString());
        registrationRequest.setPushNotificationType(PushNotificationType.FIREBASE);
        registrationRequest.setPassword(randomPassword());
        return createVolunteer(registrationRequest);
    }

    protected static String randomPhoneNumber() {
        return "+9627".concat(RandomStringUtils.secure().nextNumeric(8));
    }

    protected HospitalEntity[] fetchAvailableHospitals() throws Exception {
        String contentAsString = mockMvc.perform(MockMvcRequestBuilders.get(HOSPITAL_API).contentType("application/json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
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