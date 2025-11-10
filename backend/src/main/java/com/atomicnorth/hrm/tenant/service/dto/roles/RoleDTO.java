package com.atomicnorth.hrm.tenant.service.dto.roles;

import com.atomicnorth.hrm.tenant.domain.roles.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDTO {


    private Integer roleId;

    private String roleCode;

    private String roleName;

    private String roleNameCode;

    private String roleDescriptionCode;

    private String status;

    private LocalDateTime effectiveStartDate;

    private LocalDateTime effectiveEndDate;

    private String OpenSource;

    private String addRoleName;

    private Integer moduleId;

    private Integer functionId;

    public RoleDTO(Role role) {
        this.roleId = role.getRoleId();
        this.roleCode = role.getRoleCode();
        this.roleName = role.getRoleName();
    }

}
