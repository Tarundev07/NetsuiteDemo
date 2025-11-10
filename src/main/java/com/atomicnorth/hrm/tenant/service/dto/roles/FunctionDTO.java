package com.atomicnorth.hrm.tenant.service.dto.roles;

import lombok.Data;

import java.util.List;

@Data
public class FunctionDTO {
    private int functionId;
    private int applicationModuleId;
    private String shortCode;
    private String description;
    private String status;
    private List<FeatureDTO> child;
    private String label;
    private Integer value;

    public FunctionDTO(Integer functionId, String description) {
        this.value = functionId;
        this.label = description;
    }
}
