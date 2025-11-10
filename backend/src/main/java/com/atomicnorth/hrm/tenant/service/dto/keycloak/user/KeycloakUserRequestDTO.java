package com.atomicnorth.hrm.tenant.service.dto.keycloak.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserRequestDTO {
    private String username;
    private boolean enabled;
    private String email;
    private String firstName;
    private String lastName;
    private List<Credential> credentials;

    public KeycloakUserRequestDTO(String username, String email, String firstName, String lastName, String password) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = true; // Enable user by default

        // Set password credentials
        this.credentials = List.of(new Credential("password", password, false));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Credential {
        private String type;
        private String value;
        private boolean temporary;
    }
}

