package com.atomicnorth.hrm.tenant.service.dto.approvalflow;

import com.atomicnorth.hrm.tenant.service.dto.designation.DesignationDTO;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;

@Data
public class ApprovalFlowLevelMappingDto {

    public Integer approvalFlowLevelMappingId;

    public Integer approvalFlowLevelId;

    public Integer employeeId;

    public Integer orderBy;

    public String mailActive;

    public String smsActive;

    public String isActive;

    private List<DesignationDTO> designation;
}
