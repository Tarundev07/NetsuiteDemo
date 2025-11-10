package com.atomicnorth.hrm.tenant.service.dto.approvalflow;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
@Data
public class ApprovalFlowFunctionMappingDto {

    public Integer workFlowFunctionMappingId;

    public Integer approvalFlowId;

    public Integer functionId;

    public String byPassFlag;

    public String isActive;
}
