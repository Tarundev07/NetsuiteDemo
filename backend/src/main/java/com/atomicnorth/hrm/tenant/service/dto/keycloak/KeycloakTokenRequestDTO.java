package com.atomicnorth.hrm.tenant.service.dto.keycloak;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakTokenRequestDTO {
    private String keycloakBaseUrl;
    private String realm;
    private String clientId;
    private String grantType;
    private String username;  // Optional (for password grant)
    private String password;  // Optional (for password grant)
    private String clientSecret; // Optional (for client_credentials grant)
}

