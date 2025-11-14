package org.otherband.lifeblood.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity(name = "auth")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String hashedPassword;
    @Column(columnDefinition = "varchar(50) array")
    private Set<String> roles;
}
