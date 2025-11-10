package com.atomicnorth.hrm.tenant.service.dto.keycloak.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakRoleRequestDTO {
    private String name;
    private String description;
}
