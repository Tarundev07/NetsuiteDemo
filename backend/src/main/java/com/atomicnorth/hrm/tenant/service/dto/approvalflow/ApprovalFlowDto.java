package com.atomicnorth.hrm.tenant.service.dto.approvalflow;

import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowDelegation;
import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowFunctionMapping;
import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowLevel;
import com.atomicnorth.hrm.tenant.domain.approvalflow.ApprovalFlowRequestMapping;
import lombok.Data;

import javax.persistence.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApprovalFlowDto {

    public Integer approvalFlowId;
    public String approvalFlowName;
    public String approvalFlowCode;

    public LocalDate startDate;

    public LocalDate endDate;

    public String lastUpdateBy;

    public String createdBy;

    public LocalDateTime creationDate;

    public LocalDateTime lastUpdatedDate;

    public String sourceCategory;

    public List<ApprovalFlowFunctionMappingDto> approvalFlowFunctionMappingList;

    public List<ApprovalFlowRequestMappingDto> approvalFlowRequestMappingList;

    public List<ApprovalFlowLevelDto> approvalFlowLevelList;

    public List<ApprovalFlowDelegationDto> approvalFlowDelegationList;
}
