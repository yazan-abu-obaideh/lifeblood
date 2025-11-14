package org.otherband.lifeblood;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * This allows for mocking and testing time-related operations.
 */
@Service
public class TimeService {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    public ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }

}
