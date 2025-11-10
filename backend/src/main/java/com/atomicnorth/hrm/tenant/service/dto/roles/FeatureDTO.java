package com.atomicnorth.hrm.tenant.service.dto.roles;

import lombok.Data;

@Data
public class FeatureDTO {
    private int functionId;
    private int functionFeatureId;
    private String schemaName;
    private String shortCode;
    private String description;
    private String status;

}
