package org.otherband;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties =
        "spring.profiles.active=test"
)
public class ApplicationTest {

    @Test
    void applicationRuns() {

    }

}