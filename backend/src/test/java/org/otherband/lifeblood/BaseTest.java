package org.otherband.lifeblood;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.otherband.lifeblood.hospital.HospitalEntity;
import org.otherband.lifeblood.hospital.HospitalJpaRepository;
import org.otherband.lifeblood.volunteer.VerificationCodeJpaRepository;
import org.otherband.lifeblood.volunteer.VerificationCodeSender;
import org.otherband.lifeblood.volunteer.VolunteerJpaRepository;
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

import static org.otherband.lifeblood.hospital.HospitalController.HOSPITAL_API;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties =
        "spring.profiles.active=test"
)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
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
    public VerificationCodeSender mockVerificationCodeSender;

    @Autowired
    public VerificationCodeJpaRepository verificationCodeJpaRepository;

    @Value("classpath:hospitals.json")
    private Resource hospitals;

    @BeforeAll
    public void initialData() throws IOException {
        String hospitals = this.hospitals.getContentAsString(StandardCharsets.UTF_8);
        HospitalEntity[] hospitalEntities = objectMapper.readValue(hospitals, HospitalEntity[].class);
        hospitalJpaRepository.saveAll(Arrays.asList(hospitalEntities));
    }

    protected HospitalEntity[] fetchAvailableHospitals() throws Exception {
        String contentAsString = mockMvc.perform(
                        MockMvcRequestBuilders.get(HOSPITAL_API)
                                .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();
        return objectMapper.readValue(contentAsString,
                HospitalEntity[].class);
    }


}