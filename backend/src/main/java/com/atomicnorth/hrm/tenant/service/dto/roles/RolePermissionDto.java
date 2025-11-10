package com.atomicnorth.hrm.tenant.service.dto.roles;

import lombok.Data;

@Data
public class RolePermissionDto {
    private Long rolePermissionId;
    private Long roleId;
    private Long applicationId;
    private Long moduleId;
    private Long moduleFeatureId;
    private Long moduleFunctionId;
}
