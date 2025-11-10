package com.atomicnorth.hrm.tenant.service.dto.employement.viewDTO;

import com.atomicnorth.hrm.tenant.domain.employement.EmployeeGroup;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
public class GroupDTO {

    private String empId;

    private String isActive;

    private String createdBy;

    private String updatedBy;

    public GroupDTO(EmployeeGroup empGrp) {
        this.empId = empGrp.getEmpId();
        this.isActive = String.valueOf(empGrp.getIsActive());
        this.createdBy = empGrp.getCreatedBy();
        this.updatedBy = empGrp.getLastUpdatedBy();
    }
}
