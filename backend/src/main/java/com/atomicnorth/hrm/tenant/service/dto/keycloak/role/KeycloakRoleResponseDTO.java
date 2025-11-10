package com.atomicnorth.hrm.tenant.service.dto.keycloak.role;

import lombok.Data;

@Data
public class KeycloakRoleResponseDTO {
    private String id;
    private String name;
    private String description;
    private boolean composite;
    private boolean clientRole;
    private String containerId;
}
