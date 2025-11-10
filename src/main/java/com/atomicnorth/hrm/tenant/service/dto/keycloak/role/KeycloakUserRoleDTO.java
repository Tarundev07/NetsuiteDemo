package com.atomicnorth.hrm.tenant.service.dto.keycloak.role;

/*
public class KeycloakUserRoleDTO {
}
*/


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserRoleDTO {
    private String id;
    private String name;
    private String description;
    private boolean composite;
    private boolean clientRole;
    private String containerId;
}

