package com.atomicnorth.hrm.tenant.service.dto.roles;

import com.atomicnorth.hrm.tenant.domain.roles.Application;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionResponse {
    private boolean isError;
    private String responseCode;
    private List<Application> data;

    // Getters and Setters
}
