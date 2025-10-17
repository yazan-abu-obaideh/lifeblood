package org.otherband;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * This allows for mocking and testing time-related operations.
 */
@Service
public class TimeService {

    public LocalDateTime now() {
        return LocalDateTime.now();
    }

}
