package com.atomicnorth.hrm.tenant.service.dto.keycloak.role;

/*public class KeycloakRoleAssignToUserRequestDTO {
}*/


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakRoleAssignToUserRequestDTO {
    //@JsonProperty("id")
    //@NotBlank(message = "Role ID is required")
    private String id;

    // @JsonProperty("name")
    //@NotBlank(message = "Role name is required")
    private String name;

}




