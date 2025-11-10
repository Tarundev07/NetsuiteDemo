package com.atomicnorth.hrm.tenant.service.dto.keycloak;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakTokenResponseDTO {

    private String access_token;
    private String token_type;
    private int expires_in;
    private int refresh_expires_in;
    private String scope;

}

