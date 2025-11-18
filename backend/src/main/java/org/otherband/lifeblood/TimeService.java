package org.otherband.lifeblood;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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

    public Date toDate(LocalDateTime localDateTime) {
        return  Date.from(localDateTime.atZone(getZoneId()).toInstant());
    }

}
