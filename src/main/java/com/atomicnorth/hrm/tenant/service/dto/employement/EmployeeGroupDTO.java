package com.atomicnorth.hrm.tenant.service.dto.employement;

import com.atomicnorth.hrm.tenant.domain.Group;
import com.atomicnorth.hrm.tenant.service.dto.employement.viewDTO.GroupDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeGroupDTO {

    private List<GroupDTO> empIds;

    private Long groupId;

    private String groupCode;

    private String groupName;

    private String isActive;

    private String createdBy;

    private String updatedBy;

    public EmployeeGroupDTO(Group grp, List<GroupDTO> empIds) {
        this.groupId = grp.getId();
        this.groupCode = grp.getGroupCode();
        this.groupName = grp.getGroupName();
        this.isActive = String.valueOf(grp.getIsActive());
        this.createdBy = grp.getCreatedBy();
        this.updatedBy = grp.getLastUpdatedBy();
        this.empIds = empIds;
    }
}
