package com.atomicnorth.hrm.tenant.service.dto.roles;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationModuleDTO {
    private String label;
    private Integer value;
    private List<FunctionDTO> items;

    public ApplicationModuleDTO(String description, Integer moduleId, List<FunctionDTO> functionDTOs) {
        this.label = description;
        this.value= moduleId;
        this.items = functionDTOs;
    }
}
