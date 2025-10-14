package org.otherband;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.otherband.entity.HospitalEntity;
import org.otherband.hospital.HospitalJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SpringBootTest(properties =
        "spring.profiles.active=test"
)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    public HospitalJpaRepository hospitalJpaRepository;

    @Value("classpath:hospitals.json")
    private Resource hospitals;

    @BeforeAll
    public void initialData() throws IOException {
        String hospitals = this.hospitals.getContentAsString(StandardCharsets.UTF_8);
        HospitalEntity[] hospitalEntities = objectMapper.readValue(hospitals, HospitalEntity[].class);
        hospitalJpaRepository.saveAll(Arrays.asList(hospitalEntities));
    }

}