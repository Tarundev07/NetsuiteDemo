package com.atomicnorth.hrm.tenant.service.dto.approvalflow;

import com.atomicnorth.hrm.tenant.domain.approvalflow.WorkflowRequest;
import com.atomicnorth.hrm.tenant.domain.approvalflow.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowRequestDetailsDto {
    private WorkflowRequest workflowRequest;
    private List<WorkflowStatusDto> workflowStatusList;
}


