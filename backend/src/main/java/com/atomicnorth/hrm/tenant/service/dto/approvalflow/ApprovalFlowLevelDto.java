package com.atomicnorth.hrm.tenant.service.dto.approvalflow;


import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowLevelMapping;
import lombok.Data;

import javax.persistence.Transient;
import java.util.List;
@Data
public class ApprovalFlowLevelDto {


    public Integer approvalFlowLevelId;

    public Integer approvalFlowId;

    public String approvedBy;

    public Integer levelMasterId;

    public Integer turnAroundTime;

    public String byPassFlag;

    public String isActive;

    public Integer orderBy;

    public List<ApprovalFlowLevelMappingDto> approvalFlowLevelMappingList;
}
