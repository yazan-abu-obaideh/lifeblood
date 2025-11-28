package org.otherband.lifeblood.volunteer;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserDetails {
    private String phoneNumber;
    private List<String> roles;
}
