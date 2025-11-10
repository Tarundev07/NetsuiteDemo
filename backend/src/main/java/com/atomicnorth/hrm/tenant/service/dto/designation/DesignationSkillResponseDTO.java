package com.atomicnorth.hrm.tenant.service.dto.designation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesignationSkillResponseDTO {
    private Integer skillId;
    private String name;
    private String description;
    private String categoryCode;

}

