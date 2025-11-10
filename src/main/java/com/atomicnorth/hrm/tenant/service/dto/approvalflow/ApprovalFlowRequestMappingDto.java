package com.atomicnorth.hrm.tenant.service.dto.approvalflow;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
@Data
public class ApprovalFlowRequestMappingDto {


    public Integer workFlowRequestMappingId;

    public Integer approvalFlowId;

    public String requestedBy;

    public String requestId;
    public String byPassFlag;

    public String name;

    public String isActive;
}
