package com.atomicnorth.hrm.tenant.service.dto.roles;

import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.PastOrPresent;
import java.util.Date;
import java.util.List;

@Data
public class RoleRequest {
    private Integer roleId;
    private String roleCode;
    private String roleNameCode;
    private String roleDescriptionCode;
    private String roleName;
    private String adRoleName;
    private String roleDescription;
    @PastOrPresent(message = "Start Date cannot be in the future")
    private Date startDate;
    @FutureOrPresent(message = "End date cannot be in the past")
    private Date endDate;
    private Integer moduleId;
    private Integer functionId;
    private Long featureId;
    private String status;
    private List<RolePermissionDto> rolePermission;
    private String permission; // Optional, keep if necessary
    private Integer languageId;
}
